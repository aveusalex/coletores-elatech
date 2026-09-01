package br.com.elatech.checkoutlab.scanner

import android.content.Context

data class ScanReceipt(
    val value: String,
    val receivedAtEpochMs: Long,
    val sourceAction: String,
)

object ScanReceiptStore {
    private const val PREFERENCES = "scan_diagnostic"
    private const val VALUE = "value"
    private const val RECEIVED_AT = "received_at"
    private const val SOURCE_ACTION = "source_action"

    fun save(context: Context, receipt: ScanReceipt) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(VALUE, receipt.value)
            .putLong(RECEIVED_AT, receipt.receivedAtEpochMs)
            .putString(SOURCE_ACTION, receipt.sourceAction)
            .apply()
    }

    fun read(context: Context): ScanReceipt? {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val receivedAt = preferences.getLong(RECEIVED_AT, 0L)
        if (receivedAt == 0L) return null

        return ScanReceipt(
            value = preferences.getString(VALUE, "") ?: "",
            receivedAtEpochMs = receivedAt,
            sourceAction = preferences.getString(SOURCE_ACTION, "") ?: "",
        )
    }
}
