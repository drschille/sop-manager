package no.designsolutions.sopmanager.composeapp.qr

interface QrScanner {
    fun scan(onSuccess: (String) -> Unit, onError: (String) -> Unit)
}

expect fun provideQrScanner(): QrScanner
