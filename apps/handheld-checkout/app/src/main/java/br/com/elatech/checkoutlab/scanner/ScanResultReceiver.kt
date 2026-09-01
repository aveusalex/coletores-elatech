package br.com.elatech.checkoutlab.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ScanResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val rawValue = intent.getStringExtra(ScannerContract.DATA_KEY).orEmpty()
        val receipt = ScanReceipt(
            value = rawValue,
            receivedAtEpochMs = System.currentTimeMillis(),
            sourceAction = intent.action.orEmpty(),
        )

        ScanReceiptStore.save(context, receipt)
        Log.i(TAG, "Scanner event received; valueLength=${rawValue.length}")

        context.sendBroadcast(
            Intent(ScannerContract.ACTION_SCAN_RECEIVED).setPackage(context.packageName),
        )
    }

    private companion object {
        const val TAG = "ScanResultReceiver"
    }
}
