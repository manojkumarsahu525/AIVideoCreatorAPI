using Microsoft.EntityFrameworkCore;
using AIVideoCreatorAPI.Models;

namespace AIVideoCreatorAPI.Data
{
    public class ApplicationDbContext : DbContext
    {
        public ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : base(options)
        {
        }

        public DbSet<UserSubscription> UserSubscriptions { get; set; }
        public DbSet<Models.VideoRecord> VideoRecords { get; set; }
    }
}
