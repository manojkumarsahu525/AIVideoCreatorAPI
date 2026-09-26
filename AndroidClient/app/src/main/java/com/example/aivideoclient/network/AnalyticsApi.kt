package com.example.aivideoclient.network

import com.example.aivideoclient.model.AnalyticsResponse
import retrofit2.Response
import retrofit2.http.GET

interface AnalyticsApi {
    @GET("/api/analytics/user")
    suspend fun getUserAnalytics(): Response<AnalyticsResponse>
}
