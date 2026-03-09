package no.designsolutions.sopmanager.composeapp.logging

expect object AppLogger {
    fun init()
    fun d(message: String)
    fun i(message: String)
    fun w(message: String, throwable: Throwable? = null)
    fun e(message: String, throwable: Throwable? = null)
}
