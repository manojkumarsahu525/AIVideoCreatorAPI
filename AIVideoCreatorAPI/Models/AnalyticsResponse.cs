using System;
using System.Collections.Generic;

namespace AIVideoCreatorAPI.Models
{
    public class AnalyticsResponse
    {
        public int VideosThisMonth { get; set; }
        public double TotalGenerationTimeMs { get; set; }
        public string Plan { get; set; }
        public DateTime? ExpiryDate { get; set; }
        public List<string> WeeklyLabels { get; set; } = new List<string>();
        public List<int> WeeklyCounts { get; set; } = new List<int>();
    }
}
