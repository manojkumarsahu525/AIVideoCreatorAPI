package com.example.aivideoclient.network

import okhttp3.Interceptor
import okhttp3.Response

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val DEFAULT_MAX_RETRIES = 3
private const val DEFAULT_INITIAL_BACKOFF_MS = 500L
private const val DEFAULT_JITTER_MS = 100L

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
                val maxRetries = DEFAULT_MAX_RETRIES
                val initialBackoff = DEFAULT_INITIAL_BACKOFF_MS
                val jitterMs = DEFAULT_JITTER_MS
                while (attempt < maxRetries && !success) {
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
                    // exponential backoff with jitter
                    val backoff = initialBackoff * (1 shl (attempt - 1).coerceAtLeast(0))
                    val jitter = Random.nextLong(0, jitterMs + 1)
                    val sleepMs = backoff + jitter
                    try {
                        runBlocking { delay(sleepMs) }
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
