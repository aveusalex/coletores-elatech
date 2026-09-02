package br.com.elatech.checkoutlab.scanner

/**
 * Configuração do scanner que o **app** controla — não o Barcode Utility na mão.
 *
 * Atenção: o scanner é um recurso device-global. Um [ScannerSource] que saiba
 * aplicar isto (o do SDK) muda a config ativa do aparelho inteiro. O
 * [BroadcastScannerSource] não aplica nada (só recebe leitura). Ver ADR 0002.
 */
data class ScannerConfig(
    val beep: Beep = Beep.SOUND,
    /** 0..100. */
    val beepVolumePercent: Int = 100,
    val trigger: Trigger = Trigger.SINGLE,
    val suffix: Suffix = Suffix.NONE,
    /** Saída de dados. Para o checkout o padrão é só broadcast, sem digitar em foco. */
    val output: Output = Output.BROADCAST_ONLY,
    val enabledSymbologies: Set<Symbology> = Symbology.DEFAULTS,
) {
    enum class Beep { MUTE, SOUND, VIBRATE, SOUND_VIBRATE }

    enum class Trigger { SINGLE, CONTINUOUS, PULSE }

    enum class Suffix { NONE, ENTER, TAB, NEWLINE }

    enum class Output { BROADCAST_ONLY, BROADCAST_AND_KEYBOARD, BROADCAST_AND_CLIPBOARD }

    enum class Symbology {
        EAN_13, EAN_8, UPC_A, UPC_E, CODE_128, CODE_39, CODE_93, ITF, CODABAR,
        QR_CODE, DATA_MATRIX, PDF_417, AZTEC;

        companion object {
            val DEFAULTS: Set<Symbology> = setOf(EAN_13, EAN_8, UPC_A, CODE_128, CODE_39, QR_CODE)
        }
    }

    companion object {
        /** Perfil de partida do checkout: bip sonoro, single scan, sem sufixo, só broadcast. */
        val CHECKOUT_DEFAULT = ScannerConfig()
    }
}
