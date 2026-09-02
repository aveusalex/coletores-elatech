package br.com.elatech.checkoutlab.scanner

/**
 * Constantes do broadcast do scanner do Ranger 2N — **confirmadas no aparelho**
 * em 2026-09-01 (serviço `com.xcheng.scannere3` 2.0.8.1211), lendo EAN-13 e QR.
 *
 * Só o [BroadcastScannerSource] usa isto. Entrega: [SCAN_ACTION] é uma ação
 * **implícita** — no Android 13 precisa de
 * `Context.registerReceiver(..., RECEIVER_EXPORTED)`; receiver de manifesto é
 * bloqueado ("Background execution not allowed").
 *
 * Caminho NÃO usado: `android.intent.scanResult` / `scanKey` (tela Function
 * settings → Settings broadcast options) é **inerte nesta firmware**.
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

    /** Ações registradas em runtime. Só a principal — `_INPUT` é ignorada. */
    val OBSERVED_ACTIONS: List<String> = listOf(SCAN_ACTION)

    /** Chave do código, com poucos fallbacks genéricos antes do "primeiro String não vazio". */
    val CANDIDATE_DATA_KEYS: List<String> = listOf(DATA_KEY, "data", "barcode_string")
}
