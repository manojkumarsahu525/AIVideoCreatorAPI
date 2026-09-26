package com.example.aivideoclient.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    fun <T> createService(baseUrl: String, serviceClass: Class<T>): T {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(serviceClass)
    }

    fun create(baseUrl: String): VideoApi = createService(baseUrl, VideoApi::class.java)
}
