package br.com.elatech.checkoutlab

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import br.com.elatech.checkoutlab.scanner.ScannerConfig
import br.com.elatech.checkoutlab.scanner.ScannerConfigStore
import br.com.elatech.checkoutlab.scanner.SdkScannerSource

/**
 * Edita o [ScannerConfig], persiste no [ScannerConfigStore] e aplica no serviço
 * via [SdkScannerSource]. UI montada em código (só widgets de framework).
 */
class ScannerSettingsActivity : Activity() {

    private val store by lazy { ScannerConfigStore(this) }
    private val scanner = SdkScannerSource()

    private lateinit var beepSpinner: Spinner
    private lateinit var triggerSpinner: Spinner
    private lateinit var suffixSpinner: Spinner
    private lateinit var outputSpinner: Spinner
    private lateinit var volumeBar: SeekBar
    private lateinit var volumeLabel: TextView
    private lateinit var serviceLabel: TextView
    private val symbologyChecks = LinkedHashMap<ScannerConfig.Symbology, CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cfg = store.load()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        root.addView(title(getString(R.string.settings_title)))
        root.addView(note(getString(R.string.settings_note)))

        serviceLabel = note("")
        root.addView(serviceLabel)

        beepSpinner = enumSpinner(ScannerConfig.Beep.entries, cfg.beep)
        triggerSpinner = enumSpinner(ScannerConfig.Trigger.entries, cfg.trigger)
        suffixSpinner = enumSpinner(ScannerConfig.Suffix.entries, cfg.suffix)
        outputSpinner = enumSpinner(ScannerConfig.Output.entries, cfg.output)

        root.addView(labeled(getString(R.string.settings_beep), beepSpinner))

        volumeLabel = note(getString(R.string.settings_volume, cfg.beepVolumePercent))
        volumeBar = SeekBar(this).apply {
            max = 100
            progress = cfg.beepVolumePercent
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar, value: Int, fromUser: Boolean) {
                    volumeLabel.text = getString(R.string.settings_volume, value)
                }
                override fun onStartTrackingTouch(sb: SeekBar) = Unit
                override fun onStopTrackingTouch(sb: SeekBar) = Unit
            })
        }
        root.addView(volumeLabel)
        root.addView(volumeBar)

        root.addView(labeled(getString(R.string.settings_trigger), triggerSpinner))
        root.addView(labeled(getString(R.string.settings_suffix), suffixSpinner))
        root.addView(labeled(getString(R.string.settings_output), outputSpinner))

        root.addView(sectionLabel(getString(R.string.settings_symbologies)))
        for (sym in ScannerConfig.Symbology.entries) {
            val cb = CheckBox(this).apply {
                text = sym.name
                isChecked = sym in cfg.enabledSymbologies
            }
            symbologyChecks[sym] = cb
            root.addView(cb)
        }

        root.addView(
            Button(this).apply {
                text = getString(R.string.settings_save)
                setOnClickListener { saveAndApply() }
            },
        )

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        scanner.start(this)
        serviceLabel.text = getString(R.string.settings_service, scanner.serviceInfo() ?: "—")
    }

    override fun onPause() {
        scanner.stop(this)
        super.onPause()
    }

    private fun current(): ScannerConfig = ScannerConfig(
        beep = ScannerConfig.Beep.entries[beepSpinner.selectedItemPosition],
        beepVolumePercent = volumeBar.progress,
        trigger = ScannerConfig.Trigger.entries[triggerSpinner.selectedItemPosition],
        suffix = ScannerConfig.Suffix.entries[suffixSpinner.selectedItemPosition],
        output = ScannerConfig.Output.entries[outputSpinner.selectedItemPosition],
        enabledSymbologies = symbologyChecks.filterValues { it.isChecked }.keys.toSet(),
    )

    private fun saveAndApply() {
        val cfg = current()
        store.save(cfg)
        val applied = scanner.applyConfig(cfg)
        Toast.makeText(
            this,
            if (applied) R.string.settings_saved else R.string.settings_title,
            Toast.LENGTH_SHORT,
        ).show()
        serviceLabel.text = getString(R.string.settings_service, scanner.serviceInfo() ?: "—")
    }

    // --- helpers de UI ---

    private fun <E : Enum<E>> enumSpinner(values: List<E>, selected: E): Spinner =
        Spinner(this).apply {
            adapter = ArrayAdapter(
                this@ScannerSettingsActivity,
                android.R.layout.simple_spinner_dropdown_item,
                values.map { it.name },
            )
            setSelection(values.indexOf(selected).coerceAtLeast(0))
        }

    private fun labeled(label: String, view: View): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, dp(12), 0, 0)
        addView(sectionLabel(label))
        addView(view)
    }

    private fun title(t: String) = TextView(this).apply {
        text = t
        textSize = 22f
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun sectionLabel(t: String) = TextView(this).apply {
        text = t
        textSize = 14f
    }

    private fun note(t: String) = TextView(this).apply {
        text = t
        textSize = 12f
        setPadding(0, dp(4), 0, dp(4))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
