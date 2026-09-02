package br.com.elatech.checkoutlab

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import br.com.elatech.checkoutlab.scanner.ScanReceiptStore
import br.com.elatech.checkoutlab.scanner.ScannerBroadcastHandler
import br.com.elatech.checkoutlab.scanner.ScannerContract
import java.text.DateFormat
import java.util.Date

class MainActivity : Activity() {
    private lateinit var permissionStatus: TextView
    private lateinit var lastReading: TextView

    /** Recebe o aviso interno de que um recibo foi salvo e apenas redesenha a tela. */
    private val displayReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = renderLatestReading()
    }

    /**
     * Recebe as ações do firmware do scanner. Precisa ser registrado em runtime: no
     * Android 13 um receiver de manifesto não recebe estes broadcasts implícitos.
     */
    private val scannerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ScannerBroadcastHandler.handle(context, intent, source = "runtime")
            renderLatestReading()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionStatus = findViewById(R.id.permissionStatus)
        lastReading = findViewById(R.id.lastReading)

        findViewById<Button>(R.id.requestPermissionButton).setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
        findViewById<Button>(R.id.refreshButton).setOnClickListener { renderState() }

        renderState()
    }

    override fun onResume() {
        super.onResume()
        registerExported(displayReceiver, IntentFilter(ScannerContract.ACTION_SCAN_RECEIVED))

        val scannerFilter = IntentFilter().apply {
            ScannerContract.OBSERVED_ACTIONS.forEach { addAction(it) }
        }
        registerExported(scannerReceiver, scannerFilter)

        renderState()
    }

    override fun onPause() {
        unregisterReceiver(displayReceiver)
        unregisterReceiver(scannerReceiver)
        super.onPause()
    }

    private fun registerExported(receiver: BroadcastReceiver, filter: IntentFilter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(receiver, filter)
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
        renderLatestReading()
    }

    private fun renderLatestReading() {
        val receipt = ScanReceiptStore.read(this)
        lastReading.text = if (receipt == null) {
            getString(R.string.no_reading_yet)
        } else {
            getString(
                R.string.last_reading_template,
                receipt.value.ifBlank { getString(R.string.empty_value) },
                DateFormat.getDateTimeInstance().format(Date(receipt.receivedAtEpochMs)),
                receipt.sourceAction,
                receipt.extrasDump.ifBlank { getString(R.string.empty_value) },
            )
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
    }
}
