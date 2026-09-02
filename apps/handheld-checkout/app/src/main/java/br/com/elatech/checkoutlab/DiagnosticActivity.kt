package br.com.elatech.checkoutlab

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import br.com.elatech.checkoutlab.scanner.BroadcastScannerSource
import br.com.elatech.checkoutlab.scanner.ScanEvent
import br.com.elatech.checkoutlab.scanner.ScannerConfig
import br.com.elatech.checkoutlab.scanner.ScannerSource
import br.com.elatech.checkoutlab.scanner.SdkScannerSource
import java.text.DateFormat
import java.util.Date

/**
 * Diagnóstico do scanner. Permite alternar entre [BroadcastScannerSource] e
 * [SdkScannerSource] em execução para comparar entrega e versões no aparelho.
 */
class DiagnosticActivity : Activity() {
    private lateinit var permissionStatus: TextView
    private lateinit var lastReading: TextView
    private lateinit var sourceInfo: TextView

    private var usingSdk = false
    private var scanner: ScannerSource = BroadcastScannerSource()
    private var lastEvent: ScanEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostic)

        permissionStatus = findViewById(R.id.permissionStatus)
        lastReading = findViewById(R.id.lastReading)
        sourceInfo = findViewById(R.id.sourceInfo)

        findViewById<Button>(R.id.requestPermissionButton).setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
        findViewById<Button>(R.id.refreshButton).setOnClickListener { renderState() }
        findViewById<Button>(R.id.sourceButton).setOnClickListener { toggleSource() }

        bindListener()
    }

    override fun onResume() {
        super.onResume()
        scanner.start(this)
        renderState()
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
        if (usingSdk) scanner.applyConfig(ScannerConfig.CHECKOUT_DEFAULT)
        renderState()
    }

    private fun bindListener() {
        scanner.setListener { event ->
            lastEvent = event
            runOnUiThread { renderLatestReading() }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        renderState()
    }

    private fun renderState() {
        val granted = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        permissionStatus.text = if (granted) {
            getString(R.string.permission_granted)
        } else {
            getString(R.string.permission_pending)
        }
        sourceInfo.text = getString(
            R.string.diag_source_info,
            if (usingSdk) "SDK" else "Broadcast",
            scanner.serviceInfo() ?: "—",
        )
        renderLatestReading()
    }

    private fun renderLatestReading() {
        val event = lastEvent
        lastReading.text = if (event == null) {
            getString(R.string.no_reading_yet)
        } else {
            getString(
                R.string.last_reading_template,
                event.value.ifBlank { getString(R.string.empty_value) },
                event.symbology.ifBlank { "?" },
                DateFormat.getDateTimeInstance().format(Date(event.receivedAtEpochMs)),
                event.rawDetails.ifBlank { getString(R.string.empty_value) },
            )
        }
    }

    private companion object {
        const val CAMERA_PERMISSION_REQUEST = 1001
    }
}
