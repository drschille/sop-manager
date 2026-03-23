package no.designsolutions.sopmanager.composeapp.qr

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import no.designsolutions.sopmanager.composeapp.AndroidActivityProvider

private class AndroidQrScanner : QrScanner {
    override fun scan(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val activity = AndroidActivityProvider.currentActivity()
            ?: run {
                onError("Unable to start scanner: no active activity")
                return
            }

        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = GmsBarcodeScanning.getClient(activity, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val raw = barcode.rawValue
                if (raw.isNullOrBlank()) {
                    onError("QR code did not contain text")
                } else {
                    onSuccess(raw)
                }
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "QR scan failed")
            }
    }
}


actual fun provideQrScanner(): QrScanner = AndroidQrScanner()
