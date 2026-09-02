package br.com.elatech.checkoutlab.scanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Lógica única de tratamento de um broadcast de scanner: faz dump de todos os extras,
 * registra em log, salva o recibo e avisa a UI. Usada tanto pelo receiver de manifesto
 * (`ScanResultReceiver`, para ações explícitas) quanto pelo receiver registrado em
 * runtime na Activity (necessário para as ações implícitas do firmware no Android 13).
 */
object ScannerBroadcastHandler {
    private const val TAG = "ScanResultReceiver"

    fun handle(context: Context, intent: Intent, source: String) {
        val extras = intent.extras
        val dump = dumpIntent(intent, extras, source)
        Log.i(TAG, dump)

        val receipt = ScanReceipt(
            value = extractBarcode(extras),
            symbology = extras?.getString(ScannerContract.SYMBOLOGY_KEY).orEmpty(),
            receivedAtEpochMs = System.currentTimeMillis(),
            sourceAction = "${intent.action.orEmpty()} [$source]",
            extrasDump = dump,
        )
        ScanReceiptStore.save(context, receipt)

        context.sendBroadcast(
            Intent(ScannerContract.ACTION_SCAN_RECEIVED).setPackage(context.packageName),
        )
    }

    private fun dumpIntent(intent: Intent, extras: Bundle?, source: String): String = buildString {
        append("source=").append(source)
        append("  action=").append(intent.action.orEmpty())
        val keys = extras?.keySet().orEmpty()
        append("  |  extras=").append(keys.size)
        for (key in keys) {
            @Suppress("DEPRECATION")
            val raw = extras?.get(key)
            append("\n  ").append(key)
                .append(" (").append(raw?.javaClass?.simpleName ?: "null").append(") = ")
                .append(stringify(raw))
        }
    }

    private fun stringify(raw: Any?): String = when (raw) {
        null -> "null"
        is ByteArray -> raw.joinToString(prefix = "[", postfix = "]") { it.toInt().and(0xFF).toString() }
        is Array<*> -> raw.joinToString(prefix = "[", postfix = "]") { it?.toString().orEmpty() }
        else -> raw.toString()
    }

    private fun extractBarcode(extras: Bundle?): String {
        if (extras == null) return ""
        for (key in ScannerContract.CANDIDATE_DATA_KEYS) {
            val hit = extras.getString(key)
            if (!hit.isNullOrBlank()) return hit
        }
        for (key in extras.keySet()) {
            val hit = extras.getString(key)
            if (!hit.isNullOrBlank()) return hit
        }
        return ""
    }
}
