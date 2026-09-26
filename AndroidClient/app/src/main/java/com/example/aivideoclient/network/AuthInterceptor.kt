package com.example.aivideoclient.network

import okhttp3.Interceptor
import okhttp3.Response

object AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        val token = com.example.aivideoclient.utils.TokenRepository.currentToken
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }

        var request = builder.build()
        var response = chain.proceed(request)

        // If unauthorized, attempt to refresh token and retry once
        if (response.code == 401) {
            response.close()
            try {
                val ctx = com.example.aivideoclient.utils.AppContext.appContext
                if (ctx != null) {
                    // Refresh token synchronously
                    val newToken = kotlinx.coroutines.runBlocking {
                        com.example.aivideoclient.utils.TokenRepository.refreshToken(ctx)
                    }
                    if (!newToken.isNullOrBlank()) {
                        val newRequest = original.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                        response = chain.proceed(newRequest)
                    }
                }
            } catch (ex: Exception) {
                // ignore and return original 401
            }
        }

        return response
    }
}
