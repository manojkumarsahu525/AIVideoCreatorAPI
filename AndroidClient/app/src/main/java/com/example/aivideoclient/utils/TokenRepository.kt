package com.example.aivideoclient.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object TokenRepository {
    private const val PREFS_NAME = "aivideo_secure_prefs"
    private const val KEY_ID_TOKEN = "id_token"

    @Volatile
    var currentToken: String? = null
        private set

    fun init(context: Context) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            currentToken = prefs.getString(KEY_ID_TOKEN, null)
        } catch (ex: Exception) {
            currentToken = null
        }
    }

    suspend fun refreshToken(context: Context): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return suspendCancellableCoroutine { cont ->
            user.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token
                    currentToken = token
                    try {
                        val masterKey = MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build()
                        val prefs = EncryptedSharedPreferences.create(
                            context,
                            PREFS_NAME,
                            masterKey,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        )
                        prefs.edit().putString(KEY_ID_TOKEN, token).apply()
                    } catch (_: Exception) {
                    }
                    cont.resume(token)
                } else {
                    val ex = task.exception ?: Exception("Failed to refresh token")
                    cont.resumeWithException(ex)
                }
            }
        }
    }

    suspend fun clear(context: Context) {
        currentToken = null
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.edit().clear().apply()
        } catch (_: Exception) {
        }
    }
}
