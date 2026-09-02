package br.com.elatech.checkoutlab

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import br.com.elatech.checkoutlab.databinding.ActivityScannerSettingsBinding
import br.com.elatech.checkoutlab.scanner.ScannerConfig
import br.com.elatech.checkoutlab.scanner.ScannerConfigStore
import br.com.elatech.checkoutlab.scanner.SdkScannerSource

/** Edita, persiste e aplica o [ScannerConfig] via SDK. */
class ScannerSettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivityScannerSettingsBinding
    private val store by lazy { ScannerConfigStore(this) }
    private val scanner = SdkScannerSource()

    private val beepValues = ScannerConfig.Beep.entries
    private val triggerValues = ScannerConfig.Trigger.entries
    private val suffixValues = ScannerConfig.Suffix.entries
    private val outputValues = ScannerConfig.Output.entries
    private val symbologyValues = ScannerConfig.Symbology.entries

    private val symbologySwitches = LinkedHashMap<ScannerConfig.Symbology, MaterialSwitch>()
    private var loaded: ScannerConfig = ScannerConfig.CHECKOUT_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityScannerSettingsBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.subbar.subbarTitle.text = getString(R.string.settings_title)
        b.subbar.subbarBack.setOnClickListener { finish() }

        loaded = store.load()

        setupDropdown(b.beepInput, beepValues.map(::beepLabel), beepValues.indexOf(loaded.beep))
        setupDropdown(b.triggerInput, triggerValues.map(::triggerLabel), triggerValues.indexOf(loaded.trigger))
        setupDropdown(b.suffixInput, suffixValues.map(::suffixLabel), suffixValues.indexOf(loaded.suffix))
        setupDropdown(b.outputInput, outputValues.map(::outputLabel), outputValues.indexOf(loaded.output))

        val snapped = (loaded.beepVolumePercent / 5 * 5).coerceIn(0, 100)
        b.volumeSlider.value = snapped.toFloat()
        b.volumeValue.text = getString(R.string.settings_volume_pct, snapped)
        b.volumeSlider.addOnChangeListener { _, value, _ ->
            b.volumeValue.text = getString(R.string.settings_volume_pct, value.toInt())
            refreshDirty()
        }

        buildSymbologyRows()

        val dirtyWatcher = { refreshDirty() }
        listOf(b.beepInput, b.triggerInput, b.suffixInput, b.outputInput).forEach {
            it.setOnItemClickListener { _, _, _, _ -> refreshDirty() }
        }

        b.saveButton.setOnClickListener { saveAndApply() }
        refreshDirty()
    }

    override fun onResume() {
        super.onResume()
        scanner.start(this)
        b.root.postDelayed({ renderServiceStatus() }, 600)
        renderServiceStatus()
    }

    override fun onPause() {
        scanner.stop(this)
        super.onPause()
    }

    // ── UI ──

    private fun setupDropdown(input: MaterialAutoCompleteTextView, labels: List<String>, selected: Int) {
        input.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, labels))
        input.setText(labels[selected.coerceAtLeast(0)], false)
    }

    private fun buildSymbologyRows() {
        symbologyValues.forEach { sym ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                minimumHeight = dp(48)
            }
            val label = TextView(this).apply {
                text = sym.name.replace('_', '-')
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val sw = MaterialSwitch(this).apply {
                isChecked = sym in loaded.enabledSymbologies
                setOnCheckedChangeListener { _, _ -> refreshDirty() }
            }
            symbologySwitches[sym] = sw
            row.addView(label)
            row.addView(sw)
            b.symbologyContainer.addView(row)
        }
    }

    private fun renderServiceStatus() {
        val info = scanner.serviceInfo()
        val compatible = info?.contains("match=true") == true || info == null
        b.incompatBanner.visibility = if (compatible) View.GONE else View.VISIBLE
        b.serviceOkRow.visibility = if (compatible) View.VISIBLE else View.GONE
        b.serviceOkText.text = getString(R.string.settings_service_ok, info ?: "—")
        b.incompatBody.text = getString(R.string.settings_service_bad_body, info ?: "—")
        b.saveButton.isEnabled = compatible
    }

    private fun current(): ScannerConfig = ScannerConfig(
        beep = beepValues[indexOfLabel(b.beepInput, beepValues.map(::beepLabel))],
        beepVolumePercent = b.volumeSlider.value.toInt(),
        trigger = triggerValues[indexOfLabel(b.triggerInput, triggerValues.map(::triggerLabel))],
        suffix = suffixValues[indexOfLabel(b.suffixInput, suffixValues.map(::suffixLabel))],
        output = outputValues[indexOfLabel(b.outputInput, outputValues.map(::outputLabel))],
        enabledSymbologies = symbologySwitches.filterValues { it.isChecked }.keys.toSet(),
    )

    private fun indexOfLabel(input: MaterialAutoCompleteTextView, labels: List<String>): Int =
        labels.indexOf(input.text?.toString()).coerceAtLeast(0)

    private fun refreshDirty() {
        val dirty = current() != loaded
        b.subbar.subbarBadge.visibility = if (dirty) View.VISIBLE else View.GONE
    }

    private fun saveAndApply() {
        val cfg = current()
        store.save(cfg)
        loaded = cfg
        refreshDirty()
        val applied = scanner.applyConfig(cfg)
        Snackbar.make(
            b.root,
            if (applied) R.string.settings_saved else R.string.settings_apply_failed,
            Snackbar.LENGTH_SHORT,
        ).apply {
            if (!applied) setAction(R.string.settings_retry) { saveAndApply() }
        }.show()
        renderServiceStatus()
    }

    // ── rótulos pt-BR ──

    private fun beepLabel(v: ScannerConfig.Beep) = getString(
        when (v) {
            ScannerConfig.Beep.MUTE -> R.string.beep_mute
            ScannerConfig.Beep.SOUND -> R.string.beep_sound
            ScannerConfig.Beep.VIBRATE -> R.string.beep_vibrate
            ScannerConfig.Beep.SOUND_VIBRATE -> R.string.beep_sound_vibrate
        },
    )

    private fun triggerLabel(v: ScannerConfig.Trigger) = getString(
        when (v) {
            ScannerConfig.Trigger.SINGLE -> R.string.trigger_single
            ScannerConfig.Trigger.CONTINUOUS -> R.string.trigger_continuous
            ScannerConfig.Trigger.PULSE -> R.string.trigger_pulse
        },
    )

    private fun suffixLabel(v: ScannerConfig.Suffix) = getString(
        when (v) {
            ScannerConfig.Suffix.NONE -> R.string.suffix_none
            ScannerConfig.Suffix.ENTER -> R.string.suffix_enter
            ScannerConfig.Suffix.TAB -> R.string.suffix_tab
            ScannerConfig.Suffix.NEWLINE -> R.string.suffix_newline
        },
    )

    private fun outputLabel(v: ScannerConfig.Output) = getString(
        when (v) {
            ScannerConfig.Output.BROADCAST_ONLY -> R.string.output_broadcast_only
            ScannerConfig.Output.BROADCAST_AND_KEYBOARD -> R.string.output_broadcast_keyboard
            ScannerConfig.Output.BROADCAST_AND_CLIPBOARD -> R.string.output_broadcast_clipboard
        },
    )

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
