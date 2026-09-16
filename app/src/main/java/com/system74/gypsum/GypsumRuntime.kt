package com.system74.gypsum

import android.util.Log

object GypsumRuntime {
    private const val TAG = "GypsumRuntime"

    @Volatile
    private var cachedVersion: String? = null

    /**
     * Must be called on the main thread (e.g. from [Application.onCreate]).
     * PanamaPort arenas and downcall handles are thread-confined.
     */
    fun initialize() {
        cachedVersion = loadVersion()
    }

    fun getVersion(): String {
        cachedVersion?.let { return it }
        return loadVersion().also { cachedVersion = it }
    }

    private fun loadVersion(): String {
        checkMainThread()
        return try {
            GypsumNative.getVersion()
        } catch (e: Throwable) {
            Log.e(TAG, "FFM call failed", e)
            throw RuntimeException("Failed to call native runtime via FFM", e)
        }
    }

    private fun checkMainThread() {
        val looper = android.os.Looper.myLooper()
        if (looper != android.os.Looper.getMainLooper()) {
            throw IllegalStateException(
                "GypsumRuntime must be used on the main thread (PanamaPort FFM is thread-confined)",
            )
        }
    }
}
