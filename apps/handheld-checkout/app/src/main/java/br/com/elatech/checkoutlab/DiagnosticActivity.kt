package br.com.elatech.checkoutlab

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import br.com.elatech.checkoutlab.databinding.ActivityDiagnosticBinding
import br.com.elatech.checkoutlab.scanner.BroadcastScannerSource
import br.com.elatech.checkoutlab.scanner.ScanEvent
import br.com.elatech.checkoutlab.scanner.ScannerSource
import br.com.elatech.checkoutlab.scanner.SdkScannerSource
import java.text.DateFormat
import java.util.Date

/** Diagnóstico do scanner. Alterna Broadcast ↔ SDK em execução. */
class DiagnosticActivity : AppCompatActivity() {

    private lateinit var b: ActivityDiagnosticBinding
    private var usingSdk = false
    private var scanner: ScannerSource = BroadcastScannerSource()
    private var lastEvent: ScanEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDiagnosticBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.subbar.subbarTitle.text = getString(R.string.diag_title)
        b.subbar.subbarBack.setOnClickListener { finish() }
        b.subbar.subbarAction.visibility = android.view.View.VISIBLE
        b.subbar.subbarAction.setOnClickListener { render() }

        b.sourceButton.setOnClickListener { toggleSource() }
        bindListener()
    }

    override fun onResume() {
        super.onResume()
        scanner.start(this)
        render()
    }

    override fun onPause() {
        scanner.stop(this)
        super.onPause()
    }

    private fun toggleSource() {
        scanner.stop(this)
        usingSdk = !usingSdk
        scanner = if (usingSdk) SdkScannerSource() else BroadcastScannerSource()
        bindListener()
        scanner.start(this)
        render()
    }

    private fun bindListener() {
        scanner.setListener { event ->
            lastEvent = event
            runOnUiThread { renderReading() }
        }
    }

    private fun render() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        b.permStatus.text = getString(if (granted) R.string.permission_granted else R.string.permission_pending)
        val ok = ContextCompat.getColor(this, R.color.success)
        val pend = ContextCompat.getColor(this, R.color.warning)
        b.permStatus.setTextColor(if (granted) ok else pend)
        b.permIcon.setColorFilter(if (granted) ok else pend)
        b.permIcon.setImageResource(if (granted) R.drawable.ic_check_circle else R.drawable.ic_alert_triangle)

        b.sourceInfo.text = getString(
            R.string.diag_source_info,
            if (usingSdk) "SDK" else "Broadcast",
            scanner.serviceInfo() ?: "—",
        )
        renderReading()
    }

    private fun renderReading() {
        val e = lastEvent
        if (e == null) {
            b.lastCode.text = getString(R.string.no_reading_yet)
            b.lastMeta.text = ""
            b.lastDump.text = ""
            return
        }
        b.lastCode.text = e.value.ifBlank { getString(R.string.empty_value) }
        b.lastMeta.text = getString(
            R.string.last_reading_meta,
            e.symbology.ifBlank { "?" },
            DateFormat.getTimeInstance().format(Date(e.receivedAtEpochMs)),
        )
        b.lastDump.text = e.rawDetails.ifBlank { getString(R.string.empty_value) }
    }
}
