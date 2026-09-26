using System.Security.Claims;
using Microsoft.ApplicationInsights.Channel;
using Microsoft.ApplicationInsights.Extensibility;
using Microsoft.AspNetCore.Http;

namespace AIVideoCreatorAPI.Telemetry
{
    public class UserTelemetryInitializer : ITelemetryInitializer
    {
        private readonly IHttpContextAccessor _httpContextAccessor;

        public UserTelemetryInitializer(IHttpContextAccessor httpContextAccessor)
        {
            _httpContextAccessor = httpContextAccessor;
        }

        public void Initialize(ITelemetry telemetry)
        {
            var ctx = _httpContextAccessor.HttpContext;
            if (ctx?.User?.Identity?.IsAuthenticated == true)
            {
                var uid = ctx.User.FindFirst("firebase_uid")?.Value ?? ctx.User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                if (!string.IsNullOrWhiteSpace(uid))
                {
                    // Set authenticated user id so it appears in Application Insights
                    telemetry.Context.User.AuthenticatedUserId = uid;
                    telemetry.Context.User.Id = uid;
                }
            }
        }
    }
}
