package com.example.aivideoclient.utils

import android.content.Context

class OnboardingPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("aivideo_prefs", Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean = prefs.getBoolean("first_launch", true)

    fun setLaunched() {
        prefs.edit().putBoolean("first_launch", false).apply()
    }
}
