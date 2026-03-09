package no.designsolutions.sopmanager.composeapp.qr

private class AndroidQrScanner : QrScanner {
    override fun scan(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        onError("Camera scanning not connected yet. Wire a native scanner implementation.")
    }
}

actual fun provideQrScanner(): QrScanner = AndroidQrScanner()
