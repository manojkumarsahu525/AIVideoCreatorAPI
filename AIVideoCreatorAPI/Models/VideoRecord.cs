using System;
using System.ComponentModel.DataAnnotations;

namespace AIVideoCreatorAPI.Models
{
    public class VideoRecord
    {
        [Key]
        public int Id { get; set; }

        public string UserId { get; set; }

        public string Prompt { get; set; }

        public double DurationMs { get; set; }

        public string BlobUrl { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}
