package br.com.elatech.checkoutlab.scanner

/**
 * Contrato de scanner do Ranger 2N — **confirmado no aparelho** em 2026-09-01
 * (serviço `com.xcheng.scannere3` 2.0.8.1211), lendo EAN-13 e QR Code via
 * receiver registrado em runtime.
 *
 * Cada bip gera dois broadcasts: [SCAN_ACTION] (uso principal) e [INPUT_ACTION]
 * (caminho teclado/foco, com o mesmo dado). O app escuta só [SCAN_ACTION] para
 * ter exatamente um evento por bip.
 *
 * Entrega: no Android 13 estas ações são implícitas — precisam de
 * `Context.registerReceiver(..., RECEIVER_EXPORTED)`; receiver de manifesto é
 * bloqueado ("Background execution not allowed").
 */
object ScannerContract {
    /** Ação principal emitida pelo firmware a cada leitura. */
    const val SCAN_ACTION = "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST"

    /** Caminho teclado/foco, paralelo. Ignorado: duplicaria o evento. */
    const val INPUT_ACTION = "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST_INPUT"

    /** Ação configurável na tela Function settings. Não usada por este firmware. */
    const val LEGACY_CONFIGURABLE_ACTION = "android.intent.scanResult"

    /** Extras de [SCAN_ACTION]. */
    const val DATA_KEY = "EXTRA_BARCODE_DECODING_DATA"
    const val SYMBOLOGY_KEY = "EXTRA_BARCODE_DECODING_SYMBOLE"
    const val TIMESTAMP_START_KEY = "TIMESTAMP_START"
    const val TIMESTAMP_END_KEY = "TIMESTAMP_END"

    /** Aviso interno: um recibo de diagnóstico foi salvo. */
    const val ACTION_SCAN_RECEIVED = "br.com.elatech.checkoutlab.SCAN_RECEIVED"

    /** Ações que o app registra em runtime. */
    val OBSERVED_ACTIONS: List<String> = listOf(SCAN_ACTION, LEGACY_CONFIGURABLE_ACTION)

    /** Chaves tentadas para o valor do código, na ordem, antes do fallback. */
    val CANDIDATE_DATA_KEYS: List<String> = listOf(
        DATA_KEY,
        "scanKey",
        "EXTRA_BARCODE_STRING_DATA",
        "barcode_string",
        "data",
    )
}
