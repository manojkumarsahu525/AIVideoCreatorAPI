using System;
using System;
using System.IO;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using Azure.Storage.Blobs;
using Azure.Storage.Blobs.Models;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using System.Net.Http.Headers;
using System.Linq;

namespace AIVideoCreatorAPI.Services
{
    public class VideoService
    {
        private readonly BlobServiceClient _blobServiceClient;
        private readonly IHttpClientFactory _httpClientFactory;
        private readonly IConfiguration _configuration;
        private readonly ILogger<VideoService> _logger;

        public VideoService(BlobServiceClient blobServiceClient, IHttpClientFactory httpClientFactory, IConfiguration configuration, ILogger<VideoService> logger)
        {
            _blobServiceClient = blobServiceClient;
            _httpClientFactory = httpClientFactory;
            _configuration = configuration;
            _logger = logger;
        }

        public async Task<string> GenerateAsync(string prompt)
        {
            if (string.IsNullOrWhiteSpace(prompt))
                throw new ArgumentException("Prompt is required", nameof(prompt));

            var endpoint = _configuration["AiVideo:Endpoint"]; // full URL or relative path when a named client base address is set
            if (string.IsNullOrWhiteSpace(endpoint))
            {
                throw new InvalidOperationException("AI video endpoint is not configured. Set 'AiVideo:Endpoint' in configuration.");
            }

            var client = _httpClientFactory.CreateClient("AiVideo");

            // Prepare request payload
            var payload = new { prompt };
            var content = new StringContent(JsonSerializer.Serialize(payload), Encoding.UTF8, "application/json");

            using var cts = new CancellationTokenSource(TimeSpan.FromMinutes(5));

            HttpResponseMessage response;
            try
            {
                response = await client.PostAsync(endpoint, content, cts.Token);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending generation request to AI service.");
                throw new InvalidOperationException("Failed to send generation request to AI service.", ex);
            }

            if (!response.IsSuccessStatusCode)
            {
                var err = await response.Content.ReadAsStringAsync(cts.Token);
                _logger.LogError("AI generation request failed: {Status} {Body}", response.StatusCode, err);
                throw new InvalidOperationException($"AI generation request failed with status {response.StatusCode}.");
            }

            var respText = await response.Content.ReadAsStringAsync(cts.Token);
            using var doc = JsonDocument.Parse(respText);
            var root = doc.RootElement;

            // Try to find direct result URL
            if (TryGetStringProperty(root, new[] { "video_url", "result_url", "url" }, out var directUrl))
            {
                return await DownloadAndSaveAsync(directUrl);
            }

            // Otherwise, look for status polling info
            if (TryGetStringProperty(root, new[] { "status_url", "statusEndpoint", "status" }, out var statusUrl))
            {
                // Poll for completion
                var timeout = TimeSpan.FromMinutes(10);
                if (int.TryParse(_configuration["AiVideo:GenerationTimeoutMinutes"], out var minutes))
                {
                    timeout = TimeSpan.FromMinutes(minutes);
                }

                var pollIntervalMs = 2000;
                if (int.TryParse(_configuration["AiVideo:PollIntervalMs"], out var ms)) pollIntervalMs = ms;

                var sw = System.Diagnostics.Stopwatch.StartNew();
                while (sw.Elapsed < timeout)
                {
                    HttpResponseMessage statusResp;
                    try
                    {
                        statusResp = await client.GetAsync(statusUrl, cts.Token);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogWarning(ex, "Error polling status URL, will retry.");
                        await Task.Delay(pollIntervalMs, cts.Token);
                        continue;
                    }

                    if (!statusResp.IsSuccessStatusCode)
                    {
                        _logger.LogWarning("Status polling returned non-success {Status}", statusResp.StatusCode);
                        await Task.Delay(pollIntervalMs, cts.Token);
                        continue;
                    }

                    var statusText = await statusResp.Content.ReadAsStringAsync(cts.Token);
                    using var statusDoc = JsonDocument.Parse(statusText);
                    var statusRoot = statusDoc.RootElement;

                    // Common fields: status, result_url, video_url
                    if (TryGetStringProperty(statusRoot, new[] { "status" }, out var statusValue))
                    {
                        if (statusValue.Equals("completed", StringComparison.OrdinalIgnoreCase) || statusValue.Equals("succeeded", StringComparison.OrdinalIgnoreCase))
                        {
                            if (TryGetStringProperty(statusRoot, new[] { "result_url", "video_url", "url" }, out var resultUrl))
                            {
                                return await DownloadAndSaveAsync(resultUrl);
                            }
                            // If no direct URL provided, break and fail
                            break;
                        }

                        if (statusValue.Equals("failed", StringComparison.OrdinalIgnoreCase))
                        {
                            _logger.LogError("AI generation job failed.");
                            throw new InvalidOperationException("AI generation job failed.");
                        }
                    }

                    await Task.Delay(pollIntervalMs, cts.Token);
                }

                throw new TimeoutException("Timed out waiting for AI video generation to complete.");
            }

            // Unknown response shape
            _logger.LogError("Unexpected response shape from AI service: {Response}", respText);
            throw new InvalidOperationException("Unexpected response from AI service.");
        }

        private static bool TryGetStringProperty(JsonElement element, string[] candidates, out string? value)
        {
            foreach (var name in candidates)
            {
                if (element.TryGetProperty(name, out var prop) && prop.ValueKind == JsonValueKind.String)
                {
                    value = prop.GetString();
                    return true;
                }
            }

            // Also try to find nested properties
            foreach (var prop in element.EnumerateObject())
            {
                if (prop.Value.ValueKind == JsonValueKind.Object)
                {
                    if (TryGetStringProperty(prop.Value, candidates, out value)) return true;
                }
            }

            value = null;
            return false;
        }

        private async Task<string> DownloadAndSaveAsync(string url)
        {
            if (string.IsNullOrWhiteSpace(url)) throw new ArgumentException("Result URL is empty.", nameof(url));

            var client = _httpClientFactory.CreateClient("AiVideo");

            HttpResponseMessage downloadResp;
            try
            {
                downloadResp = await client.GetAsync(url, HttpCompletionOption.ResponseHeadersRead);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error downloading generated video from {Url}", url);
                throw new InvalidOperationException("Failed to download generated video.", ex);
            }

            if (!downloadResp.IsSuccessStatusCode)
            {
                _logger.LogError("Failed to download generated video. Status: {Status}", downloadResp.StatusCode);
                throw new InvalidOperationException($"Failed to download generated video. Status: {downloadResp.StatusCode}");
            }

            await using var stream = await downloadResp.Content.ReadAsStreamAsync();
            var fileName = $"{Guid.NewGuid()}.mp4";
            return await SaveVideoAsync(stream, fileName);
        }

        public async Task<string> SaveVideoAsync(Stream videoStream, string fileName)
        {
            var containerClient = _blobServiceClient.GetBlobContainerClient("videos");

            // Create the container if it doesn't exist and make blobs publicly readable
            await containerClient.CreateIfNotExistsAsync(PublicAccessType.Blob);

            var blobClient = containerClient.GetBlobClient(fileName);

            // Reset stream position if possible
            if (videoStream.CanSeek)
            {
                videoStream.Position = 0;
            }

            var headers = new BlobHttpHeaders { ContentType = "video/mp4" };
            await blobClient.UploadAsync(videoStream, headers);

            return blobClient.Uri.ToString();
        }
    }
}
