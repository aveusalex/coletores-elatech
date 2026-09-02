package br.com.elatech.checkoutlab.scanner

/**
 * Uma leitura entregue por um [ScannerSource], independente do transporte
 * (broadcast do firmware hoje, callback do SDK depois).
 */
data class ScanEvent(
    /** Conteúdo decodificado do código. */
    val value: String,
    /** Simbologia informada pelo scanner (`EAN-13`, `QRCODE`, ...), vazia se ausente. */
    val symbology: String,
    /** Início/fim da decodificação, em epoch ms, quando o transporte informa. */
    val startedAtEpochMs: Long?,
    val endedAtEpochMs: Long?,
    /** Quando o app recebeu o evento. */
    val receivedAtEpochMs: Long,
    /** Dump cru dos extras/campos originais, só para diagnóstico. */
    val rawDetails: String,
)
