using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace AIVideoCreatorAPI.Migrations
{
    public partial class InitialCreate : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "UserSubscriptions",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    UserId = table.Column<string>(type: "TEXT", nullable: true),
                    Plan = table.Column<string>(type: "TEXT", nullable: true),
                    Status = table.Column<string>(type: "TEXT", nullable: true),
                    ExpiryDate = table.Column<DateTime>(type: "TEXT", nullable: true),
                    StripeCustomerId = table.Column<string>(type: "TEXT", nullable: true),
                    StripeSubscriptionId = table.Column<string>(type: "TEXT", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_UserSubscriptions", x => x.Id);
                });

            migrationBuilder.CreateTable(
                name: "VideoRecords",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    UserId = table.Column<string>(type: "TEXT", nullable: true),
                    Prompt = table.Column<string>(type: "TEXT", nullable: true),
                    DurationMs = table.Column<double>(type: "REAL", nullable: false),
                    BlobUrl = table.Column<string>(type: "TEXT", nullable: true),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_VideoRecords", x => x.Id);
                });
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "VideoRecords");

            migrationBuilder.DropTable(
                name: "UserSubscriptions");
        }
    }
}
