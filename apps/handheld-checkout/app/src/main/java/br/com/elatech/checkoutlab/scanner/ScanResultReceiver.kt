package br.com.elatech.checkoutlab.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver de manifesto. No Android 13 só recebe broadcasts explícitos (com componente
 * ou pacote alvo); as ações implícitas do firmware chegam pelo receiver registrado em
 * runtime na Activity. A lógica é a mesma nos dois casos: [ScannerBroadcastHandler].
 */
class ScanResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ScannerBroadcastHandler.handle(context, intent, source = "manifest")
    }
}
