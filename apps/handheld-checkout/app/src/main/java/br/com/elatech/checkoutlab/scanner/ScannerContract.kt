package br.com.elatech.checkoutlab.scanner

/** Valores observados no Barcode Utility do Ranger 2N em 2026-09-01. */
object ScannerContract {
    const val SCAN_ACTION = "android.intent.scanResult"
    const val DATA_KEY = "scanKey"
    const val ACTION_SCAN_RECEIVED = "br.com.elatech.checkoutlab.SCAN_RECEIVED"
}
