package br.com.elatech.checkoutlab.scanner

import android.content.Context

/** Recebe [ScanEvent]s. `fun interface` para permitir lambda. */
fun interface ScanListener {
    fun onScan(event: ScanEvent)
}

/**
 * Costura entre o app e o mecanismo de scanner. O domínio (catálogo, carrinho,
 * venda) depende só desta interface, nunca do broadcast nem do SDK.
 *
 * Implementações:
 * - [BroadcastScannerSource] — recebe o broadcast do firmware. Não configura o
 *   scanner. É o transporte atual, provado na Fase 3.
 * - `SdkScannerSource` (Fase 3.5, sob autorização) — usa `XcBarcodeScanner`:
 *   recebe por callback e aplica [ScannerConfig] de verdade.
 */
interface ScannerSource {

    /** Começa a receber leituras. Idempotente. */
    fun start(context: Context)

    /** Para de receber. Idempotente. */
    fun stop(context: Context)

    /** Define (ou limpa) o destino das leituras. */
    fun setListener(listener: ScanListener?)

    /**
     * Aplica a configuração do scanner. Fontes que não controlam o hardware
     * (ex.: broadcast) devem registrar em log e ignorar, retornando `false`.
     *
     * @return `true` se a configuração foi realmente aplicada ao serviço.
     */
    fun applyConfig(config: ScannerConfig): Boolean

    /** Versão do serviço/SDK para diagnóstico, ou `null` se indisponível. */
    fun serviceInfo(): String?
}
