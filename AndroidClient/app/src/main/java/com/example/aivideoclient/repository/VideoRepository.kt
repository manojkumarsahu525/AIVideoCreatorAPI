package com.example.aivideoclient.repository

import com.example.aivideoclient.model.VideoRequest
import com.example.aivideoclient.network.VideoApi

class VideoRepository(private val api: VideoApi) {
    suspend fun generateVideo(prompt: String): Result<String> {
        return try {
            val resp = api.generate(VideoRequest(prompt))
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body?.videoUrl != null) Result.success(body.videoUrl)
                else Result.failure(Exception("Empty response body"))
            } else {
                val msg = resp.errorBody()?.string() ?: resp.message()
                Result.failure(Exception("API error: $msg"))
            }
        } catch (ex: Exception) {
            Result.failure(ex)
        }
    }
}
