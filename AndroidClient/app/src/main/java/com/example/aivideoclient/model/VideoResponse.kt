package com.example.aivideoclient.model

import com.google.gson.annotations.SerializedName

data class VideoResponse(
    @SerializedName("videoUrl") val videoUrl: String
)
