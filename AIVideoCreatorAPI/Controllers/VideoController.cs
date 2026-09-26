using Microsoft.AspNetCore.Mvc;

namespace AIVideoCreatorAPI.Controllers
{
    using Microsoft.AspNetCore.Mvc;
    using Microsoft.AspNetCore.Authorization;
    using AIVideoCreatorAPI.Models;
    using AIVideoCreatorAPI.Services;
    using Microsoft.ApplicationInsights;
    using Microsoft.Extensions.Logging;
    using System.Security.Claims;
    using System.Collections.Generic;
    using System.Linq;
    using System;

    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class VideoController : ControllerBase
    {
        private readonly VideoService _videoService;
        private readonly TelemetryClient _telemetry;
        private readonly ILogger<VideoController> _logger;
        private readonly AIVideoCreatorAPI.Data.ApplicationDbContext _db;

        public VideoController(VideoService videoService, TelemetryClient telemetry, ILogger<VideoController> logger, AIVideoCreatorAPI.Data.ApplicationDbContext db)
        {
            _videoService = videoService;
            _telemetry = telemetry;
            _logger = logger;
            _db = db;
        }

        [HttpPost("generate")]
        public async Task<IActionResult> GenerateVideo([FromBody] VideoRequest request)
        {
            if (request == null || string.IsNullOrWhiteSpace(request.Prompt))
            {
                return BadRequest(new { error = "Prompt is required." });
            }

            var uid = User.FindFirst("firebase_uid")?.Value ?? User.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? string.Empty;

            var startProps = new Dictionary<string, string>
            {
                { "uid", uid },
                { "prompt", request.Prompt }
            };

            try
            {
                var sw = System.Diagnostics.Stopwatch.StartNew();
                // Check subscription status
                var sub = _db.UserSubscriptions.FirstOrDefault(s => s.UserId == uid && s.Status == "active");
                if (sub == null || (sub.ExpiryDate.HasValue && sub.ExpiryDate.Value <= DateTime.UtcNow))
                {
                    _logger.LogInformation("User {uid} attempted video generation without active subscription", uid);
                    return StatusCode(402, new { error = "Payment required: active subscription needed" });
                }

                _telemetry.TrackEvent("VideoGenerationStarted", startProps);
                _logger.LogInformation("Video generation started for UID: {uid}", uid);

                var blobUrl = await _videoService.GenerateAsync(request.Prompt);
                sw.Stop();
                var durationMs = sw.Elapsed.TotalMilliseconds;

                // Persist video record for analytics
                try
                {
                    var record = new AIVideoCreatorAPI.Models.VideoRecord
                    {
                        UserId = uid,
                        Prompt = request.Prompt,
                        DurationMs = durationMs,
                        BlobUrl = blobUrl,
                        CreatedAt = DateTime.UtcNow
                    };
                    _db.VideoRecords.Add(record);
                    await _db.SaveChangesAsync();
                }
                catch (Exception ex)
                {
                    _logger.LogWarning(ex, "Failed to save video record for analytics");
                }

                // Add duration to telemetry
                var completedProps = new Dictionary<string, string>(startProps)
                {
                    { "blobUrl", blobUrl }
                };
                _telemetry.TrackMetric("VideoGenerationDurationMs", durationMs);
                completedProps["durationMs"] = durationMs.ToString();

                _telemetry.TrackEvent("VideoGenerationCompleted", completedProps);
                _logger.LogInformation("Video generation completed for UID: {uid}, url: {url}, durationMs: {d}", uid, blobUrl, durationMs);

                return Ok(new { videoUrl = blobUrl, durationMs });
            }
            catch (Exception ex)
            {
                _telemetry.TrackException(ex, startProps);
                _logger.LogError(ex, "Video generation failed for UID: {uid}", uid);
                return StatusCode(500, new { error = ex.Message });
            }
        }
    }
}
