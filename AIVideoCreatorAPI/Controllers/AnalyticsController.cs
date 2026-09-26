using System;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authorization;
using AIVideoCreatorAPI.Data;
using AIVideoCreatorAPI.Models;
using Microsoft.ApplicationInsights;

namespace AIVideoCreatorAPI.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class AnalyticsController : ControllerBase
    {
        private readonly ApplicationDbContext _db;
        private readonly TelemetryClient _telemetry;

        public AnalyticsController(ApplicationDbContext db, TelemetryClient telemetry)
        {
            _db = db;
            _telemetry = telemetry;
        }

        [HttpGet("user")]
        public async Task<IActionResult> GetUserAnalytics()
        {
            var uid = User.FindFirst("firebase_uid")?.Value ?? User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrWhiteSpace(uid)) return Unauthorized();

            var now = DateTime.UtcNow;
            var monthAgo = now.AddMonths(-1);

            // Videos this month
            var videosThisMonth = await _db.VideoRecords.Where(v => v.UserId == uid && v.CreatedAt >= monthAgo).CountAsync();

            // Total generation time (ms) this month
            var totalMs = await _db.VideoRecords.Where(v => v.UserId == uid && v.CreatedAt >= monthAgo).SumAsync(v => (double?)v.DurationMs) ?? 0.0;

            // Subscription info
            var sub = await _db.UserSubscriptions.Where(s => s.UserId == uid).OrderByDescending(s => s.Id).FirstOrDefaultAsync();

            // Weekly counts for last 4 weeks
            var weeks = Enumerable.Range(0, 4).Select(i => new { Start = now.Date.AddDays(-7 * (3 - i)), End = now.Date.AddDays(-7 * (3 - i) + 7) }).ToList();
            var labels = weeks.Select(w => w.Start.ToString("MM-dd")).ToList();
            var counts = new System.Collections.Generic.List<int>();
            foreach (var w in weeks)
            {
                var c = await _db.VideoRecords.Where(v => v.UserId == uid && v.CreatedAt >= w.Start && v.CreatedAt < w.End).CountAsync();
                counts.Add(c);
            }

            var resp = new AnalyticsResponse
            {
                VideosThisMonth = videosThisMonth,
                TotalGenerationTimeMs = totalMs,
                Plan = sub?.Plan,
                ExpiryDate = sub?.ExpiryDate,
                WeeklyLabels = labels,
                WeeklyCounts = counts
            };

            // Track that analytics were requested
            _telemetry?.TrackEvent("AnalyticsViewed", new System.Collections.Generic.Dictionary<string, string> { { "userId", uid } });

            return Ok(resp);
        }
    }
}
