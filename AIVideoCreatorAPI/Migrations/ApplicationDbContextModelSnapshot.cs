using System;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;
using AIVideoCreatorAPI.Data;

#nullable disable

namespace AIVideoCreatorAPI.Migrations
{
    [DbContext(typeof(ApplicationDbContext))]
    partial class ApplicationDbContextModelSnapshot : ModelSnapshot
    {
        protected override void BuildModel(ModelBuilder modelBuilder)
        {
            modelBuilder
                .HasAnnotation("ProductVersion", "8.0.0")
                .HasAnnotation("Relational:MaxIdentifierLength", 63);

            modelBuilder.Entity("AIVideoCreatorAPI.Models.UserSubscription", b =>
            {
                b.Property<int>("Id").ValueGeneratedOnAdd().HasColumnType("INTEGER");
                b.Property<string>("UserId").HasColumnType("TEXT");
                b.Property<string>("Plan").HasColumnType("TEXT");
                b.Property<string>("Status").HasColumnType("TEXT");
                b.Property<DateTime?>("ExpiryDate").HasColumnType("TEXT");
                b.Property<string>("StripeCustomerId").HasColumnType("TEXT");
                b.Property<string>("StripeSubscriptionId").HasColumnType("TEXT");

                b.HasKey("Id");

                b.ToTable("UserSubscriptions");
            });

            modelBuilder.Entity("AIVideoCreatorAPI.Models.VideoRecord", b =>
            {
                b.Property<int>("Id").ValueGeneratedOnAdd().HasColumnType("INTEGER");
                b.Property<string>("UserId").HasColumnType("TEXT");
                b.Property<string>("Prompt").HasColumnType("TEXT");
                b.Property<double>("DurationMs").HasColumnType("REAL");
                b.Property<string>("BlobUrl").HasColumnType("TEXT");
                b.Property<DateTime>("CreatedAt").HasColumnType("TEXT");

                b.HasKey("Id");

                b.ToTable("VideoRecords");
            });
        }
    }
}
