using Microsoft.AspNetCore.Mvc;

namespace AIVideoCreatorAPI.Controllers
{
    using Microsoft.AspNetCore.Mvc;
    using AIVideoCreatorAPI.Models;
    using AIVideoCreatorAPI.Services;

    [ApiController]
    [Route("api/[controller]")]
    public class VideoController : ControllerBase
    {
        private readonly VideoService _videoService;

        public VideoController(VideoService videoService)
        {
            _videoService = videoService;
        }

        [HttpPost("generate")]
        public async Task<IActionResult> GenerateVideo([FromBody] VideoRequest request)
        {
            if (request == null || string.IsNullOrWhiteSpace(request.Prompt))
            {
                return BadRequest(new { error = "Prompt is required." });
            }

            try
            {
                var blobUrl = await _videoService.GenerateAsync(request.Prompt);
                return Ok(new { videoUrl = blobUrl });
            }
            catch (Exception ex)
            {
                // Log and return error
                return StatusCode(500, new { error = ex.Message });
            }
        }
    }
}
