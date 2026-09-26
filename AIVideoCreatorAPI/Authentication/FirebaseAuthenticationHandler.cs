using System;
using System;
using System.Security.Claims;
using System.Text.Encodings.Web;
using System.Threading.Tasks;
using FirebaseAdmin.Auth;
using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace AIVideoCreatorAPI.Authentication
{
    public class FirebaseAuthenticationHandler : AuthenticationHandler<AuthenticationSchemeOptions>
    {
        private readonly IConfiguration _configuration;

        public FirebaseAuthenticationHandler(
            IOptionsMonitor<AuthenticationSchemeOptions> options,
            ILoggerFactory logger,
            UrlEncoder encoder,
            ISystemClock clock,
            IConfiguration configuration)
            : base(options, logger, encoder, clock)
        {
            _configuration = configuration;
        }

        protected override async Task<AuthenticateResult> HandleAuthenticateAsync()
        {
            if (!Request.Headers.ContainsKey("Authorization"))
            {
                return AuthenticateResult.Fail("Missing Authorization Header");
            }

            var authHeader = Request.Headers["Authorization"].ToString();
            if (string.IsNullOrWhiteSpace(authHeader) || !authHeader.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase))
            {
                return AuthenticateResult.Fail("Invalid Authorization Header");
            }

            var token = authHeader.Substring("Bearer ".Length).Trim();
            if (string.IsNullOrWhiteSpace(token))
            {
                return AuthenticateResult.Fail("Missing Bearer Token");
            }

            try
            {
                // Ensure FirebaseApp is initialized in Program.cs
                if (FirebaseAdmin.FirebaseApp.DefaultInstance == null)
                {
                    Logger.LogWarning("FirebaseApp is not initialized. Ensure Firebase Admin SDK is initialized in Program.cs");
                    return AuthenticateResult.Fail("Firebase not configured");
                }

                var auth = FirebaseAuth.GetAuth(FirebaseAdmin.FirebaseApp.DefaultInstance);
                var decodedToken = await auth.VerifyIdTokenAsync(token);

                var uid = decodedToken.Uid ?? string.Empty;

                // Create claims principal
                var claims = new[]
                {
                    new Claim(ClaimTypes.NameIdentifier, uid),
                    // include firebase uid as a custom claim
                    new Claim("firebase_uid", uid),
                };

                var identity = new ClaimsIdentity(claims, Scheme.Name);
                var principal = new ClaimsPrincipal(identity);
                var ticket = new AuthenticationTicket(principal, Scheme.Name);

                Logger.LogInformation("Firebase token validated for UID: {uid}", uid);

                return AuthenticateResult.Success(ticket);
            }
            catch (FirebaseAuthException ex)
            {
                Logger.LogWarning(ex, "Invalid Firebase token");
                return AuthenticateResult.Fail("Invalid Firebase token");
            }
            catch (Exception ex)
            {
                Logger.LogError(ex, "Error while validating Firebase token");
                return AuthenticateResult.Fail("Error validating token");
            }
        }
    }
}
