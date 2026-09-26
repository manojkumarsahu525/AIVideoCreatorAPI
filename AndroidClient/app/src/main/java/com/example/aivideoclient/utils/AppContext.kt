package com.example.aivideoclient.utils

import android.content.Context

object AppContext {
    @Volatile
    var appContext: Context? = null
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
