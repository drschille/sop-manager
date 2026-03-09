package no.designsolutions.sopmanager.composeapp.logging

actual object AppLogger {
    private const val TAG = "SopMobile"

    actual fun init() {
        i("Logger initialized")
    }

    actual fun d(message: String) {
        println("D/$TAG: $message")
    }

    actual fun i(message: String) {
        println("I/$TAG: $message")
    }

    actual fun w(message: String, throwable: Throwable?) {
        println("W/$TAG: $message")
        throwable?.let { println("W/$TAG: ${it.message}") }
    }

    actual fun e(message: String, throwable: Throwable?) {
        println("E/$TAG: $message")
        throwable?.let { println("E/$TAG: ${it.message}") }
    }
}
