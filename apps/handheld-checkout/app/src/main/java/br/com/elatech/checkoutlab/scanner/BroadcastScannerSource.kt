package br.com.elatech.checkoutlab.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log

/**
 * [ScannerSource] sobre o broadcast do firmware do Ranger 2N.
 *
 * Ação [ScannerContract.SCAN_ACTION], registrada **em runtime** com
 * `RECEIVER_EXPORTED` — receiver de manifesto é barrado no Android 13.
 * Não configura o scanner: [applyConfig] só loga e devolve `false`.
 */
class BroadcastScannerSource : ScannerSource {

    private var listener: ScanListener? = null
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val event = parse(intent) ?: return
            Log.i(TAG, "scan value.len=${event.value.length} symbology=${event.symbology}")
            listener?.onScan(event)
        }
    }

    override fun start(context: Context) {
        if (registered) return
        val filter = IntentFilter().apply {
            ScannerContract.OBSERVED_ACTIONS.forEach { addAction(it) }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        registered = true
    }

    override fun stop(context: Context) {
        if (!registered) return
        runCatching { context.unregisterReceiver(receiver) }
        registered = false
    }

    override fun setListener(listener: ScanListener?) {
        this.listener = listener
    }

    override fun applyConfig(config: ScannerConfig): Boolean {
        Log.w(TAG, "applyConfig ignorado: fonte broadcast não controla o scanner ($config)")
        return false
    }

    override fun serviceInfo(): String? = null

    private fun parse(intent: Intent): ScanEvent? {
        val extras = intent.extras ?: return null
        val value = extractValue(extras)
        if (value.isBlank()) return null
        return ScanEvent(
            value = value,
            symbology = extras.getString(ScannerContract.SYMBOLOGY_KEY).orEmpty(),
            startedAtEpochMs = extras.takeIfHas(ScannerContract.TIMESTAMP_START_KEY),
            endedAtEpochMs = extras.takeIfHas(ScannerContract.TIMESTAMP_END_KEY),
            receivedAtEpochMs = System.currentTimeMillis(),
            rawDetails = dump(intent, extras),
        )
    }

    private fun extractValue(extras: Bundle): String {
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

    private fun Bundle.takeIfHas(key: String): Long? =
        if (containsKey(key)) getLong(key) else null

    private fun dump(intent: Intent, extras: Bundle): String = buildString {
        append("action=").append(intent.action.orEmpty())
        append("  extras=").append(extras.keySet().size)
        for (key in extras.keySet()) {
            @Suppress("DEPRECATION")
            val raw = extras.get(key)
            append("\n  ").append(key)
                .append(" (").append(raw?.javaClass?.simpleName ?: "null").append(") = ")
                .append(raw?.toString() ?: "null")
        }
    }

    private companion object {
        const val TAG = "BroadcastScannerSource"
    }
}
