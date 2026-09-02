package br.com.elatech.checkoutlab.scanner

/**
 * Contrato de scanner do Ranger 2N — **confirmado no aparelho** em 2026-09-01
 * (serviço `com.xcheng.scannere3` 2.0.8.1211), lendo EAN-13 e QR Code via
 * receiver registrado em runtime.
 *
 * Entrega: [SCAN_ACTION] é uma ação **implícita**. No Android 13 precisa de
 * `Context.registerReceiver(..., RECEIVER_EXPORTED)`; receiver de manifesto é
 * bloqueado ("Background execution not allowed").
 *
 * Caminho NÃO usado: a ação configurável `android.intent.scanResult` com a chave
 * `scanKey` (tela Function settings → Settings broadcast options) é **inerte
 * nesta firmware** — nunca é emitida. Não configurar nem depender dela.
 */
object ScannerContract {
    /** Ação principal emitida pelo firmware a cada leitura. */
    const val SCAN_ACTION = "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST"

    /** Caminho teclado/foco, paralelo. Ignorado: duplicaria o evento. */
    const val INPUT_ACTION = "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST_INPUT"

    /** Extras de [SCAN_ACTION]. */
    const val DATA_KEY = "EXTRA_BARCODE_DECODING_DATA"
    const val SYMBOLOGY_KEY = "EXTRA_BARCODE_DECODING_SYMBOLE"
    const val TIMESTAMP_START_KEY = "TIMESTAMP_START"
    const val TIMESTAMP_END_KEY = "TIMESTAMP_END"

    /** Aviso interno: um recibo de diagnóstico foi salvo. */
    const val ACTION_SCAN_RECEIVED = "br.com.elatech.checkoutlab.SCAN_RECEIVED"

    /** Ações que o app registra em runtime. Só a principal — `_INPUT` é ignorada. */
    val OBSERVED_ACTIONS: List<String> = listOf(SCAN_ACTION)

    /** Chave do código, com poucos fallbacks genéricos antes do "primeiro String não vazio". */
    val CANDIDATE_DATA_KEYS: List<String> = listOf(DATA_KEY, "data", "barcode_string")
}
