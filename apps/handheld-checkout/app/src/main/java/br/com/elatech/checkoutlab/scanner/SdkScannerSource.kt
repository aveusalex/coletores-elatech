package br.com.elatech.checkoutlab.scanner

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.xcheng.scanner.BarcodeType
import com.xcheng.scanner.NotificationType
import com.xcheng.scanner.OutputMethod
import com.xcheng.scanner.ScannerSymResult
import com.xcheng.scanner.XcBarcodeScanner

/**
 * [ScannerSource] usando o XCScanner SDK (`com.xcheng.scanner`, aar vendorado —
 * ver `app/libs/PROVENANCE.md`). Recebe leitura por callback e **aplica**
 * [ScannerConfig] no serviço (device-global — ver ADR 0002).
 *
 * `init`/`deInit` fazem bind/unbind do serviço AIDL. O callback do SDK vem em
 * thread de binder; esta classe entrega o [ScanEvent] já na thread principal.
 */
class SdkScannerSource : ScannerSource {

    private val main = Handler(Looper.getMainLooper())
    private var listener: ScanListener? = null
    private var started = false
    private var appContext: Context? = null

    private val symResult = ScannerSymResult { sym, content ->
        val value = content.orEmpty()
        if (value.isEmpty()) return@ScannerSymResult
        Log.i(TAG, "onResult sym=$sym len=${value.length}")
        val event = ScanEvent(
            value = value,
            symbology = sym.orEmpty(),
            startedAtEpochMs = null,
            endedAtEpochMs = null,
            receivedAtEpochMs = System.currentTimeMillis(),
            rawDetails = "sdk onResult · sym=$sym",
        )
        main.post { listener?.onScan(event) }
    }

    override fun start(context: Context) {
        if (started) return
        appContext = context.applicationContext
        XcBarcodeScanner.init(context.applicationContext, symResult)
        started = true
        Log.i(TAG, "init · ${describeVersions(context.applicationContext)}")
    }

    override fun stop(context: Context) {
        if (!started) return
        runCatching { XcBarcodeScanner.deInit(context.applicationContext) }
        started = false
    }

    override fun setListener(listener: ScanListener?) {
        this.listener = listener
    }

    override fun applyConfig(config: ScannerConfig): Boolean = runCatching {
        XcBarcodeScanner.setOutputMethod(
            when (config.output) {
                ScannerConfig.Output.BROADCAST_ONLY -> OutputMethod.BROADCAST
                ScannerConfig.Output.BROADCAST_AND_KEYBOARD -> OutputMethod.BROADCAST_KEYBOARD
                ScannerConfig.Output.BROADCAST_AND_CLIPBOARD -> OutputMethod.BROADCAST_CLIPBOARD
            },
        )
        XcBarcodeScanner.setSuccessNotification(
            when (config.beep) {
                ScannerConfig.Beep.MUTE -> NotificationType.MUTE
                ScannerConfig.Beep.SOUND -> NotificationType.SOUND
                ScannerConfig.Beep.VIBRATE -> NotificationType.VIBRATOR
                ScannerConfig.Beep.SOUND_VIBRATE -> NotificationType.SOUND_VIBRATOR
            },
        )
        XcBarcodeScanner.setScanVolume(config.beepVolumePercent.coerceIn(0, 100) / 100f)
        XcBarcodeScanner.setScanMode(
            if (config.trigger == ScannerConfig.Trigger.CONTINUOUS) "REPEAT_SCAN" else "SINGLE_SCAN",
        )
        XcBarcodeScanner.setTextSuffix(
            when (config.suffix) {
                ScannerConfig.Suffix.NONE -> "Empty"
                ScannerConfig.Suffix.ENTER, ScannerConfig.Suffix.NEWLINE -> "\n"
                ScannerConfig.Suffix.TAB -> "\t"
            },
        )
        config.enabledSymbologies.forEach { sym ->
            barcodeTypeOf(sym)?.let { XcBarcodeScanner.enableBarcodeType(it, true) }
        }
        XcBarcodeScanner.saveSettings()
        Log.i(TAG, "applyConfig ok · $config")
        true
    }.getOrElse { e ->
        Log.e(TAG, "applyConfig falhou", e)
        false
    }

    override fun serviceInfo(): String? = appContext?.let { ctx ->
        runCatching { describeVersions(ctx) }.getOrNull()
    }

    private fun describeVersions(context: Context): String = runCatching {
        val sdk = XcBarcodeScanner.getSdkVersion(context)
        val svc = XcBarcodeScanner.getServiceVersion().orEmpty()
        if (svc.isEmpty()) {
            "sdk=$sdk service=? (serviço ainda não reportou — reconsultar)"
        } else {
            "sdk=$sdk service=$svc match=${versionMatches(sdk, svc)}"
        }
    }.getOrElse { "versões indisponíveis (${it.message})" }

    private fun barcodeTypeOf(sym: ScannerConfig.Symbology): String? = when (sym) {
        ScannerConfig.Symbology.EAN_13 -> BarcodeType.EAN13
        ScannerConfig.Symbology.EAN_8 -> BarcodeType.EAN8
        ScannerConfig.Symbology.UPC_A -> BarcodeType.UPCA
        ScannerConfig.Symbology.UPC_E -> BarcodeType.UPCE
        ScannerConfig.Symbology.CODE_128 -> BarcodeType.CODE128
        ScannerConfig.Symbology.CODE_39 -> BarcodeType.CODE39
        ScannerConfig.Symbology.CODE_93 -> BarcodeType.CODE93
        ScannerConfig.Symbology.ITF -> BarcodeType.ITF25
        ScannerConfig.Symbology.CODABAR -> BarcodeType.CODABAR
        ScannerConfig.Symbology.QR_CODE -> BarcodeType.QRCODE
        ScannerConfig.Symbology.DATA_MATRIX -> BarcodeType.DATAMATRIX
        ScannerConfig.Symbology.PDF_417 -> BarcodeType.PDF417
        ScannerConfig.Symbology.AZTEC -> BarcodeType.AZTEC
    }

    private companion object {
        const val TAG = "SdkScannerSource"

        /**
         * Regra da tabela de mapeamento do SDK: para versões >= 1.3.49.0.13, o
         * serviço serve se `service >= sdk`. Comparação lexicográfica por campo.
         */
        fun versionMatches(sdk: String?, service: String?): Boolean {
            if (sdk.isNullOrEmpty() || service.isNullOrEmpty()) return false
            val a = service.split(".").map { it.toIntOrNull() ?: 0 }
            val b = sdk.split(".").map { it.toIntOrNull() ?: 0 }
            for (i in 0 until maxOf(a.size, b.size)) {
                val x = a.getOrElse(i) { 0 }
                val y = b.getOrElse(i) { 0 }
                if (x != y) return x > y
            }
            return true
        }
    }
}
