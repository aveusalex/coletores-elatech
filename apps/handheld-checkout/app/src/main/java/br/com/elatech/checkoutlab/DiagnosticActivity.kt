package br.com.elatech.checkoutlab

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import br.com.elatech.checkoutlab.scanner.BroadcastScannerSource
import br.com.elatech.checkoutlab.scanner.ScanEvent
import br.com.elatech.checkoutlab.scanner.ScannerSource
import java.text.DateFormat
import java.util.Date

/** Tela de diagnóstico do scanner. Roda sobre a mesma costura [ScannerSource]. */
class DiagnosticActivity : Activity() {
    private lateinit var permissionStatus: TextView
    private lateinit var lastReading: TextView

    private val scanner: ScannerSource = BroadcastScannerSource()
    private var lastEvent: ScanEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostic)

        permissionStatus = findViewById(R.id.permissionStatus)
        lastReading = findViewById(R.id.lastReading)

        findViewById<Button>(R.id.requestPermissionButton).setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
        findViewById<Button>(R.id.refreshButton).setOnClickListener { renderState() }

        scanner.setListener { event ->
            lastEvent = event
            runOnUiThread { renderLatestReading() }
        }
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
