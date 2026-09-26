package com.example.aivideoclient.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "aivideo_prefs")

object OnboardingDataStore {
    private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")

    suspend fun isFirstLaunch(context: Context): Boolean {
        val prefs = try {
            context.dataStore.data.catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }.first()
        } catch (ex: Exception) {
            return true
        }
        return prefs[FIRST_LAUNCH] ?: true
    }

    suspend fun setLaunched(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[FIRST_LAUNCH] = false
        }
    }
}
