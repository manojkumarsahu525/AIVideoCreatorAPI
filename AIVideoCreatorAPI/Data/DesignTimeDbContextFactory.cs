using System;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;
using Microsoft.Extensions.Configuration;

namespace AIVideoCreatorAPI.Data
{
    public class DesignTimeDbContextFactory : IDesignTimeDbContextFactory<ApplicationDbContext>
    {
        public ApplicationDbContext CreateDbContext(string[] args)
        {
            var builder = new DbContextOptionsBuilder<ApplicationDbContext>();

            // Try to read connection string from environment variable, fallback to SQLite file
            var conn = Environment.GetEnvironmentVariable("ConnectionStrings__DefaultConnection");
            if (string.IsNullOrWhiteSpace(conn))
            {
                conn = "Data Source=./aivideocreator.db";
                builder.UseSqlite(conn);
            }
            else
            {
                builder.UseSqlServer(conn);
            }

            return new ApplicationDbContext(builder.Options);
        }
    }
}
