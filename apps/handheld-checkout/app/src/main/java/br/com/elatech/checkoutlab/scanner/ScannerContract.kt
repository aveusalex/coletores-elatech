package br.com.elatech.checkoutlab.scanner

/**
 * Valores de broadcast do scanner do Ranger 2N.
 *
 * `SCAN_ACTION` / `DATA_KEY` são os campos configuráveis observados no Barcode Utility
 * (Function settings) em 2026-09-01. Na prova de 2026-09-01 eles não entregaram evento
 * ao app: o serviço (`2.0.8.1211`) emite as ações `OBSERVED_ACTIONS` abaixo, capturadas
 * por logcat. O diagnóstico v0.2.0 escuta todas elas e faz dump de todos os extras para
 * descobrir a chave real do dado.
 */
object ScannerContract {
    const val SCAN_ACTION = "android.intent.scanResult"
    const val DATA_KEY = "scanKey"
    const val ACTION_SCAN_RECEIVED = "br.com.elatech.checkoutlab.SCAN_RECEIVED"

    /** Ações emitidas de fato por `com.xcheng.scannere3` neste firmware (logcat 2026-09-01). */
    val OBSERVED_ACTIONS: List<String> = listOf(
        SCAN_ACTION,
        "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST",
        "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST_INPUT",
    )

    /**
     * Chaves candidatas para o valor do código, tentadas em ordem antes do fallback
     * "primeiro extra String não vazio". Sem chute cego: o dump registra todas as chaves
     * reais para confirmação.
     */
    val CANDIDATE_DATA_KEYS: List<String> = listOf(
        DATA_KEY,
        "EXTRA_BARCODE_DECODING_DATA",
        "EXTRA_BARCODE_STRING_DATA",
        "barcode_string",
        "data",
        "barcode",
        "SCAN_BARCODE1",
        "scannerdata",
    )
}
