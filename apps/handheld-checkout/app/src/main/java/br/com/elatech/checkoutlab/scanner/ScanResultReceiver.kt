package br.com.elatech.checkoutlab.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Recebe as ações de scanner observadas no Ranger 2N e registra **todos** os extras do
 * intent (chave, tipo, valor) para diagnóstico. Não interpreta nem valida o código; só
 * expõe o que chega, para descobrir o contrato real de entrega.
 */
class ScanResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        val dump = dumpIntent(intent, extras)
        Log.i(TAG, dump)

        val value = extractBarcode(extras)
        val receipt = ScanReceipt(
            value = value,
            receivedAtEpochMs = System.currentTimeMillis(),
            sourceAction = intent.action.orEmpty(),
            extrasDump = dump,
        )

        ScanReceiptStore.save(context, receipt)

        context.sendBroadcast(
            Intent(ScannerContract.ACTION_SCAN_RECEIVED).setPackage(context.packageName),
        )
    }

    private fun dumpIntent(intent: Intent, extras: Bundle?): String = buildString {
        append("action=").append(intent.action.orEmpty())
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
        // Fallback: primeiro extra String não vazio.
        for (key in extras.keySet()) {
            val hit = extras.getString(key)
            if (!hit.isNullOrBlank()) return hit
        }
        return ""
    }

    private companion object {
        const val TAG = "ScanResultReceiver"
    }
}
