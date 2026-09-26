using System;
using Microsoft.EntityFrameworkCore;
using AIVideoCreatorAPI.Data;
using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;
using Microsoft.ApplicationInsights.Extensibility;

using System;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();
// Application Insights
builder.Services.AddApplicationInsightsTelemetry(options =>
{
    options.ConnectionString = builder.Configuration["ApplicationInsights:ConnectionString"];
});
// Configure EF Core (SQLite) for subscription storage
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection") ?? builder.Configuration["ConnectionStrings:DefaultConnection"];
if (string.IsNullOrWhiteSpace(connectionString))
{
    // default to a local file for development
    connectionString = "Data Source=./aivideocreator.db";
}

builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseSqlite(connectionString));

// Ensure database is created at startup
builder.Services.AddHostedService<AIVideoCreatorAPI.Data.DatabaseInitializerHostedService>();
// Register HttpContextAccessor and telemetry initializer to include user UID in telemetry
builder.Services.AddHttpContextAccessor();
builder.Services.AddSingleton<ITelemetryInitializer, AIVideoCreatorAPI.Telemetry.UserTelemetryInitializer>();
// Add authentication scheme that validates Firebase ID tokens
builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = "Firebase";
    options.DefaultChallengeScheme = "Firebase";
})
    .AddScheme<Microsoft.AspNetCore.Authentication.AuthenticationSchemeOptions, AIVideoCreatorAPI.Authentication.FirebaseAuthenticationHandler>(
        "Firebase", options => { });
// Register HttpClient for AI video service
builder.Services.AddHttpClient("AiVideo", client =>
{
    var baseUrl = builder.Configuration["AiVideo:BaseUrl"];
    if (!string.IsNullOrWhiteSpace(baseUrl))
    {
        client.BaseAddress = new Uri(baseUrl);
    }

    var apiKey = builder.Configuration["AiVideo:ApiKey"];
    if (!string.IsNullOrWhiteSpace(apiKey))
    {
        client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", apiKey);
    }

    client.Timeout = TimeSpan.FromMinutes(10);
});
// Configure Azure Blob Storage
var blobConnectionString = builder.Configuration["AzureBlobStorage:ConnectionString"];
if (string.IsNullOrWhiteSpace(blobConnectionString))
{
    throw new InvalidOperationException("Azure Blob Storage connection string is not configured. Set 'AzureBlobStorage:ConnectionString' in configuration or environment variables.");
}

builder.Services.AddSingleton(new Azure.Storage.Blobs.BlobServiceClient(blobConnectionString));
builder.Services.AddSingleton<AIVideoCreatorAPI.Services.VideoService>();

// Initialize Firebase Admin SDK
var firebaseCredPath = builder.Configuration["Firebase:CredentialFilePath"];
if (!string.IsNullOrWhiteSpace(firebaseCredPath))
{
    var googleCred = GoogleCredential.FromFile(firebaseCredPath);
    FirebaseApp.Create(new AppOptions { Credential = googleCred });
}
else
{
    // Try to create default app which will use ADC (GOOGLE_APPLICATION_CREDENTIALS env var) if available
    try
    {
        FirebaseApp.Create();
    }
    catch (Exception)
    {
        // If already created or no default credentials available, ignore here.
    }
}
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Ensure telemetry is flushed on shutdown
try
{
    var telemetryClient = app.Services.GetRequiredService<Microsoft.ApplicationInsights.TelemetryClient>();
    app.Lifetime.ApplicationStopping.Register(() =>
    {
        telemetryClient.Flush();
        // give time for flush
        System.Threading.Thread.Sleep(2000);
    });
}
catch
{
    // If Application Insights is not configured, ignore
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

app.UseAuthentication();

app.UseAuthorization();

app.MapControllers();

app.Run();
