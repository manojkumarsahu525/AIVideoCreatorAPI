package com.example.aivideoclient.network

import okhttp3.Interceptor
import okhttp3.Response

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay

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

        // If unauthorized, attempt to refresh token with exponential backoff and retry up to 3 times
        if (response.code == 401) {
            response.close()
            val ctx = com.example.aivideoclient.utils.AppContext.appContext
            if (ctx != null) {
                var attempt = 0
                var success = false
                var lastException: Exception? = null
                while (attempt < 3 && !success) {
                    try {
                        val newToken = runBlocking { com.example.aivideoclient.utils.TokenRepository.refreshToken(ctx) }
                        if (!newToken.isNullOrBlank()) {
                            val newRequest = original.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()
                            response = chain.proceed(newRequest)
                            success = response.code != 401
                            if (success) break
                        }
                    } catch (ex: Exception) {
                        lastException = ex
                    }

                    attempt++
                    // exponential backoff: 500ms, 1000ms, 2000ms
                    val backoff = 500L * (1 shl (attempt - 1).coerceAtLeast(0))
                    try {
                        runBlocking { delay(backoff) }
                    } catch (_: Exception) { }
                }

                if (!success && lastException != null) {
                    // swallow exception but allow original 401 to surface
                }
            }
        }

        return response
    }
}
