package no.designsolutions.sopmanager.composeapp.qr

private class IosQrScanner : QrScanner {
    override fun scan(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        onError("Camera scanning not connected yet. Wire an AVFoundation scanner implementation.")
    }
}

actual fun provideQrScanner(): QrScanner = IosQrScanner()
