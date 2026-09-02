package br.com.elatech.checkoutlab.scanner

import android.content.Context

data class ScanReceipt(
    val value: String,
    val symbology: String,
    val receivedAtEpochMs: Long,
    val sourceAction: String,
    val extrasDump: String,
)

object ScanReceiptStore {
    private const val PREFERENCES = "scan_diagnostic"
    private const val VALUE = "value"
    private const val SYMBOLOGY = "symbology"
    private const val RECEIVED_AT = "received_at"
    private const val SOURCE_ACTION = "source_action"
    private const val EXTRAS_DUMP = "extras_dump"

    fun save(context: Context, receipt: ScanReceipt) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putString(VALUE, receipt.value)
            .putString(SYMBOLOGY, receipt.symbology)
            .putLong(RECEIVED_AT, receipt.receivedAtEpochMs)
            .putString(SOURCE_ACTION, receipt.sourceAction)
            .putString(EXTRAS_DUMP, receipt.extrasDump)
            .apply()
    }

    fun read(context: Context): ScanReceipt? {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val receivedAt = preferences.getLong(RECEIVED_AT, 0L)
        if (receivedAt == 0L) return null

        return ScanReceipt(
            value = preferences.getString(VALUE, "") ?: "",
            symbology = preferences.getString(SYMBOLOGY, "") ?: "",
            receivedAtEpochMs = receivedAt,
            sourceAction = preferences.getString(SOURCE_ACTION, "") ?: "",
            extrasDump = preferences.getString(EXTRAS_DUMP, "") ?: "",
        )
    }
}
