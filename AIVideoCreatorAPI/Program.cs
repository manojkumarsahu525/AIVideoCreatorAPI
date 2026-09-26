using System;

using System;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();
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
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();
