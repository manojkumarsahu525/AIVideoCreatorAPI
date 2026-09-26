package com.example.aivideoclient.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun create(baseUrl: String): VideoApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(VideoApi::class.java)
    }
}
