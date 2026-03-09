package no.designsolutions.sopmanager.composeapp.logging

import android.util.Log

actual object AppLogger {
    private const val TAG = "SopMobile"

    actual fun init() {
        i("Logger initialized")
    }

    actual fun d(message: String) {
        Log.d(TAG, message)
    }

    actual fun i(message: String) {
        Log.i(TAG, message)
    }

    actual fun w(message: String, throwable: Throwable?) {
        Log.w(TAG, message, throwable)
    }

    actual fun e(message: String, throwable: Throwable?) {
        Log.e(TAG, message, throwable)
    }
}
