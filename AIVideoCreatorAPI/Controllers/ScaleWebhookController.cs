using Microsoft.AspNetCore.Mvc;
using Microsoft.ApplicationInsights;
using Microsoft.Extensions.Logging;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;

namespace AIVideoCreatorAPI.Controllers
{
    [ApiController]
    [Route("api/scale")]
    public class ScaleWebhookController : ControllerBase
    {
        private readonly TelemetryClient _telemetry;
        private readonly ILogger<ScaleWebhookController> _logger;

        public ScaleWebhookController(TelemetryClient telemetry, ILogger<ScaleWebhookController> logger)
        {
            _telemetry = telemetry;
            _logger = logger;
        }

        // Azure Action Group webhook will POST JSON payload for alerts
        [HttpPost("notify")]
        [AllowAnonymous]
        public async Task<IActionResult> Notify([FromBody] object payload)
        {
            // Log raw payload to telemetry for auditing
            _telemetry.TrackEvent("ScaleNotificationReceived", properties: new System.Collections.Generic.Dictionary<string, string>
            {
                { "contentType", Request.ContentType ?? string.Empty }
            }, metrics: null);

            _telemetry.TrackTrace("Scale notification received");
            _telemetry.TrackMetric("ScaleNotificationPayloadSize", Request.ContentLength ?? 0);

            _logger.LogInformation("Received scale notification: {payload}", payload);

            // Optionally parse known fields if needed for richer telemetry
            return Ok();
        }
    }
}
