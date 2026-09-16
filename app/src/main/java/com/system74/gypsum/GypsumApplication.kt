package com.system74.gypsum

import android.app.Application
import android.util.Log

class GypsumApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // PanamaPort / FFM must be initialized on the main thread.
        try {
            GypsumRuntime.initialize()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Gypsum runtime", e)
        }
    }

    companion object {
        private const val TAG = "GypsumApplication"
    }
}
