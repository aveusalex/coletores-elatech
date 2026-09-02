package br.com.elatech.checkoutlab.scanner

import android.content.Context

/**
 * Persiste [ScannerConfig] em `SharedPreferences`. Enums viram o nome; o conjunto
 * de simbologias vira CSV. Valores desconhecidos caem no default.
 */
class ScannerConfigStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("scanner_config", Context.MODE_PRIVATE)

    fun load(): ScannerConfig {
        val d = ScannerConfig.CHECKOUT_DEFAULT
        return ScannerConfig(
            beep = prefs.enum("beep", d.beep),
            beepVolumePercent = prefs.getInt("beepVolumePercent", d.beepVolumePercent).coerceIn(0, 100),
            trigger = prefs.enum("trigger", d.trigger),
            suffix = prefs.enum("suffix", d.suffix),
            output = prefs.enum("output", d.output),
            enabledSymbologies = prefs.getString("symbologies", null)
                ?.split(",")
                ?.mapNotNull { name -> runCatching { ScannerConfig.Symbology.valueOf(name) }.getOrNull() }
                ?.toSet()
                ?.ifEmpty { d.enabledSymbologies }
                ?: d.enabledSymbologies,
        )
    }

    fun save(config: ScannerConfig) {
        prefs.edit()
            .putString("beep", config.beep.name)
            .putInt("beepVolumePercent", config.beepVolumePercent)
            .putString("trigger", config.trigger.name)
            .putString("suffix", config.suffix.name)
            .putString("output", config.output.name)
            .putString("symbologies", config.enabledSymbologies.joinToString(",") { it.name })
            .apply()
    }

    private inline fun <reified E : Enum<E>> android.content.SharedPreferences.enum(
        key: String,
        default: E,
    ): E = getString(key, null)?.let { runCatching { enumValueOf<E>(it) }.getOrNull() } ?: default
}
