package com.example.aivideoclient.network

import com.example.aivideoclient.model.VideoRequest
import com.example.aivideoclient.model.VideoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VideoApi {
    @POST("/api/video/generate")
    suspend fun generate(@Body request: VideoRequest): Response<VideoResponse>
}
