package com.example.aivideoclient.model

import com.google.gson.annotations.SerializedName

data class AnalyticsResponse(
    @SerializedName("videosThisMonth") val videosThisMonth: Int,
    @SerializedName("totalGenerationTimeMs") val totalGenerationTimeMs: Double,
    @SerializedName("plan") val plan: String?,
    @SerializedName("expiryDate") val expiryDate: String?,
    // weeklyLabels aligned with weeklyCounts
    @SerializedName("weeklyLabels") val weeklyLabels: List<String> = emptyList(),
    @SerializedName("weeklyCounts") val weeklyCounts: List<Int> = emptyList()
)
