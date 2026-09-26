package com.example.aivideoclient.utils

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val Context.dataStore by preferencesDataStore(name = "aivideo_token")

object TokenRepository {
    private val ID_TOKEN = preferencesKey<String>("id_token")

    @Volatile
    var currentToken: String? = null
        private set

    fun init(context: Context) {
        // load token synchronously on init
        try {
            val prefs = runBlocking {
                context.dataStore.data.catch { exception ->
                    if (exception is IOException) emit(emptyPreferences()) else throw exception
                }.first()
            }
            currentToken = prefs[ID_TOKEN]
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
                        // persist token
                        kotlinx.coroutines.GlobalScope.launch {
                            context.dataStore.edit { prefs ->
                                if (token != null) prefs[ID_TOKEN] = token
                            }
                        }
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
        context.dataStore.edit { prefs -> prefs.clear() }
    }
}
