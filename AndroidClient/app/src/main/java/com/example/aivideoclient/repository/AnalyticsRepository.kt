package com.example.aivideoclient.repository

import com.example.aivideoclient.model.AnalyticsResponse
import com.example.aivideoclient.network.AnalyticsApi

class AnalyticsRepository(private val api: AnalyticsApi) {
    suspend fun getUserAnalytics(): Result<AnalyticsResponse> {
        return try {
            val resp = api.getUserAnalytics()
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null) Result.success(body)
                else Result.failure(Exception("Empty analytics response"))
            } else {
                val msg = resp.errorBody()?.string() ?: resp.message()
                Result.failure(Exception("API error: $msg"))
            }
        } catch (ex: Exception) {
            Result.failure(ex)
        }
    }
}
