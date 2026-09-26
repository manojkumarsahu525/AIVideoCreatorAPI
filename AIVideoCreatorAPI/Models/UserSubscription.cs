using System;
using System.ComponentModel.DataAnnotations;

namespace AIVideoCreatorAPI.Models
{
    public class UserSubscription
    {
        [Key]
        public int Id { get; set; }

        public string UserId { get; set; }

        public string Plan { get; set; }

        public string Status { get; set; }

        public DateTime? ExpiryDate { get; set; }

        public string StripeCustomerId { get; set; }

        public string StripeSubscriptionId { get; set; }
    }
}
