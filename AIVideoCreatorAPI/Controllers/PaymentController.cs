using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Stripe;
using Stripe.Checkout;
using Stripe.Infrastructure;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using AIVideoCreatorAPI.Data;
using System.Linq;
using AIVideoCreatorAPI.Models;
using System;
using Microsoft.EntityFrameworkCore;

namespace AIVideoCreatorAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class PaymentController : ControllerBase
    {
        private readonly IConfiguration _configuration;
        private readonly ILogger<PaymentController> _logger;
        private readonly ApplicationDbContext _db;
        private readonly Microsoft.ApplicationInsights.TelemetryClient _telemetry;

        public PaymentController(IConfiguration configuration, ILogger<PaymentController> logger, ApplicationDbContext db, Microsoft.ApplicationInsights.TelemetryClient telemetry)
        {
            _configuration = configuration;
            _logger = logger;
            _db = db;
            _telemetry = telemetry;

            StripeConfiguration.ApiKey = _configuration["Stripe:SecretKey"];
        }

        [HttpPost("create-checkout-session")]
        [Authorize]
        public async Task<IActionResult> CreateCheckoutSession([FromBody] CreateCheckoutRequest req)
        {
            var uid = User.FindFirst("firebase_uid")?.Value ?? User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrWhiteSpace(uid)) return Unauthorized();

            var priceId = _configuration[$"Stripe:Prices:{req.Plan}"];
            if (string.IsNullOrWhiteSpace(priceId)) return BadRequest(new { error = "Unknown plan" });

            var options = new SessionCreateOptions
            {
                Mode = "subscription",
                LineItems = new System.Collections.Generic.List<SessionLineItemOptions>
                {
                    new SessionLineItemOptions { Price = priceId, Quantity = 1 }
                },
                SuccessUrl = req.SuccessUrl ?? _configuration["Stripe:SuccessUrl"],
                CancelUrl = req.CancelUrl ?? _configuration["Stripe:CancelUrl"],
                ClientReferenceId = uid,
                CustomerEmail = req.Email // optional
            };

            var service = new SessionService();
            var session = await service.CreateAsync(options);

            // Store or update subscription record with pending status and associate Stripe customer later via webhook
            var sub = await _db.UserSubscriptions.FirstOrDefaultAsync(s => s.UserId == uid);
            if (sub == null)
            {
                sub = new UserSubscription { UserId = uid, Plan = req.Plan, Status = "pending" };
                _db.UserSubscriptions.Add(sub);
            }
            else
            {
                sub.Plan = req.Plan;
                sub.Status = "pending";
            }
            await _db.SaveChangesAsync();

            return Ok(new { sessionUrl = session.Url, sessionId = session.Id });
        }

        [HttpPost("webhook")]
        [AllowAnonymous]
        public async Task<IActionResult> Webhook()
        {
            var json = await new System.IO.StreamReader(HttpContext.Request.Body).ReadToEndAsync();
            var stripeSignature = Request.Headers["Stripe-Signature"].FirstOrDefault();
            var webhookSecret = _configuration["Stripe:WebhookSecret"];

            Event stripeEvent;
            try
            {
                stripeEvent = EventUtility.ConstructEvent(json, stripeSignature, webhookSecret);
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Unable to construct Stripe event");
                return BadRequest();
            }

            _logger.LogInformation("Received Stripe event: {type}", stripeEvent.Type);

            try
            {
                if (stripeEvent.Type == Events.CheckoutSessionCompleted)
                {
                    var session = stripeEvent.Data.Object as Session;
                    // retrieve customer and subscription
                    var customerId = session.CustomerId;
                    var subscriptionId = session.SubscriptionId;
                    var clientRef = session.ClientReferenceId; // our uid

                    if (!string.IsNullOrWhiteSpace(clientRef))
                    {
                        var sub = await _db.UserSubscriptions.FirstOrDefaultAsync(s => s.UserId == clientRef);
                        if (sub != null)
                        {
                            sub.Status = "active";
                            sub.StripeCustomerId = customerId;
                            sub.StripeSubscriptionId = subscriptionId;
                            sub.ExpiryDate = null; // ongoing
                            await _db.SaveChangesAsync();
                        }
                        else
                        {
                            // create record
                            sub = new UserSubscription
                            {
                                UserId = clientRef,
                                Plan = "unknown",
                                Status = "active",
                                StripeCustomerId = customerId,
                                StripeSubscriptionId = subscriptionId
                            };
                            _db.UserSubscriptions.Add(sub);
                            await _db.SaveChangesAsync();
                        }

                        // Track subscription activation telemetry
                        try
                        {
                            var props = new System.Collections.Generic.Dictionary<string, string>
                            {
                                { "userId", clientRef },
                                { "plan", sub.Plan ?? "unknown" },
                                { "status", sub.Status },
                                { "stripeCustomerId", customerId ?? string.Empty },
                                { "stripeSubscriptionId", subscriptionId ?? string.Empty }
                            };
                            _telemetry?.TrackEvent("SubscriptionActivated", props);
                        }
                        catch (System.Exception ex)
                        {
                            _logger.LogWarning(ex, "Failed to track subscription telemetry");
                        }
                    }
                }
                else if (stripeEvent.Type == Events.InvoicePaymentFailed)
                {
                    var invoice = stripeEvent.Data.Object as Invoice;
                    var customerId = invoice.CustomerId;
                    var sub = await _db.UserSubscriptions.FirstOrDefaultAsync(s => s.StripeCustomerId == customerId || s.StripeSubscriptionId == invoice.SubscriptionId);
                    if (sub != null)
                    {
                        sub.Status = "past_due";
                        await _db.SaveChangesAsync();

                        try
                        {
                            var props = new System.Collections.Generic.Dictionary<string, string>
                            {
                                { "userId", sub.UserId },
                                { "plan", sub.Plan ?? "unknown" },
                                { "status", sub.Status }
                            };
                            // Invoice may have amount_paid (in cents)
                            if (invoice.AmountPaid > 0)
                            {
                                var amount = invoice.AmountPaid / 100.0;
                                _telemetry?.TrackMetric("SubscriptionRevenue", amount);
                                props["amountPaid"] = amount.ToString();
                            }
                            _telemetry?.TrackEvent("SubscriptionPaymentFailed", props);
                        }
                        catch (System.Exception ex)
                        {
                            _logger.LogWarning(ex, "Failed to track failed payment telemetry");
                        }
                    }
                }
                else if (stripeEvent.Type == Events.CustomerSubscriptionDeleted || stripeEvent.Type == Events.CustomerSubscriptionUpdated)
                {
                    var subscription = stripeEvent.Data.Object as Subscription;
                    var customerId = subscription.CustomerId;
                    var sub = await _db.UserSubscriptions.FirstOrDefaultAsync(s => s.StripeSubscriptionId == subscription.Id || s.StripeCustomerId == customerId);
                    if (sub != null)
                    {
                        sub.Status = subscription.Status;
                        // CurrentPeriodEnd is DateTime in Stripe.NET; set expiry if cancellation scheduled
                        if (subscription.CancelAtPeriodEnd == true && subscription.CurrentPeriodEnd != default)
                        {
                            sub.ExpiryDate = subscription.CurrentPeriodEnd;
                        }
                        await _db.SaveChangesAsync();

                        try
                        {
                            var props = new System.Collections.Generic.Dictionary<string, string>
                            {
                                { "userId", sub.UserId },
                                { "plan", sub.Plan ?? "unknown" },
                                { "status", sub.Status },
                                { "stripeSubscriptionId", subscription.Id }
                            };
                            _telemetry?.TrackEvent("SubscriptionUpdated", props);
                        }
                        catch (System.Exception ex)
                        {
                            _logger.LogWarning(ex, "Failed to track subscription update telemetry");
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing Stripe webhook");
            }

            return Ok();
        }
    }

    public class CreateCheckoutRequest
    {
        public string Plan { get; set; }
        public string Email { get; set; }
        public string SuccessUrl { get; set; }
        public string CancelUrl { get; set; }
    }
}
