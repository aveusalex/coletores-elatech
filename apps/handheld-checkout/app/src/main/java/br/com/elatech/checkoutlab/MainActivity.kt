package br.com.elatech.checkoutlab

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import br.com.elatech.checkoutlab.checkout.CheckoutController
import br.com.elatech.checkoutlab.data.AppDatabase
import br.com.elatech.checkoutlab.data.RoomCatalog
import br.com.elatech.checkoutlab.data.RoomSaleHistory
import br.com.elatech.checkoutlab.domain.CartLine
import br.com.elatech.checkoutlab.domain.Money
import br.com.elatech.checkoutlab.domain.ScanOutcome
import br.com.elatech.checkoutlab.scanner.ScannerConfigStore
import br.com.elatech.checkoutlab.scanner.SdkScannerSource

/**
 * Tela de checkout offline. Bipe adiciona/incrementa; código desconhecido abre
 * cadastro de produto fictício; "Finalizar" registra venda simulada e limpa.
 * Catálogo e vendas em Room; scanner via SDK com [ScannerConfigStore].
 */
class MainActivity : Activity() {

    private val controller: CheckoutController by lazy {
        val db = AppDatabase.get(this)
        CheckoutController(
            scanner = SdkScannerSource(),
            catalog = RoomCatalog(db.productDao()),
            history = RoomSaleHistory(db.saleDao()),
        )
    }

    private lateinit var totalValue: TextView
    private lateinit var itemCount: TextView
    private lateinit var outcomeStatus: TextView
    private lateinit var cartLines: LinearLayout
    private lateinit var emptyHint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        totalValue = findViewById(R.id.totalValue)
        itemCount = findViewById(R.id.itemCount)
        outcomeStatus = findViewById(R.id.outcomeStatus)
        cartLines = findViewById(R.id.cartLines)
        emptyHint = findViewById(R.id.emptyHint)

        findViewById<Button>(R.id.finishButton).setOnClickListener { finishSale() }
        findViewById<Button>(R.id.clearButton).setOnClickListener {
            controller.cart.clear()
            renderCart()
        }
        findViewById<Button>(R.id.diagnosticButton).setOnClickListener {
            startActivity(Intent(this, DiagnosticActivity::class.java))
        }
        findViewById<Button>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, ScannerSettingsActivity::class.java))
        }
        findViewById<Button>(R.id.historyButton).setOnClickListener {
            startActivity(Intent(this, SalesHistoryActivity::class.java))
        }

        controller.onCartChanged = { runOnUiThread { renderCart() } }
        controller.onOutcome = { outcome -> runOnUiThread { renderOutcome(outcome) } }

        outcomeStatus.text = getString(R.string.checkout_outcome_none)
    }

    override fun onResume() {
        super.onResume()
        controller.scannerConfig = ScannerConfigStore(this).load()
        controller.attach(this)
        renderCart()
    }

    override fun onPause() {
        controller.detach(this)
        super.onPause()
    }

    private fun renderOutcome(outcome: ScanOutcome) {
        outcomeStatus.text = when (outcome) {
            is ScanOutcome.Added ->
                getString(R.string.checkout_outcome_added, outcome.line.product.name)
            is ScanOutcome.Incremented ->
                getString(
                    R.string.checkout_outcome_incremented,
                    outcome.line.product.name,
                    outcome.line.quantity,
                )
            is ScanOutcome.Unknown -> {
                promptRegister(outcome.code)
                getString(R.string.checkout_outcome_unknown, outcome.code)
            }
        }
    }

    private fun renderCart() {
        totalValue.text = controller.cart.total.formatBRL()
        itemCount.text = getString(R.string.checkout_items_label, controller.cart.itemCount)
        emptyHint.visibility = if (controller.cart.isEmpty) View.VISIBLE else View.GONE

        cartLines.removeAllViews()
        for (line in controller.cart.items) {
            cartLines.addView(cartLineView(line))
        }
    }

    private fun cartLineView(line: CartLine): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(10), 0, dp(10))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }

        val info = TextView(this).apply {
            text = "${line.product.name}\n" +
                "${getString(R.string.checkout_line_qty, line.quantity)} · ${line.lineTotal.formatBRL()}"
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(info)

        row.addView(stepButton("−") { controller.setQuantity(line.product.sku, line.quantity - 1) })
        row.addView(stepButton("+") { controller.setQuantity(line.product.sku, line.quantity + 1) })
        row.addView(
            Button(this).apply {
                text = getString(R.string.checkout_remove)
                setOnClickListener { controller.remove(line.product.sku) }
            },
        )
        return row
    }

    private fun stepButton(label: String, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            minWidth = dp(48)
            setOnClickListener { onClick() }
        }

    private fun promptRegister(code: String) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), dp(8))
        }
        val nameField = EditText(this).apply {
            hint = getString(R.string.unknown_name_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
        val priceField = EditText(this).apply {
            hint = getString(R.string.unknown_price_hint)
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        container.addView(nameField)
        container.addView(priceField)

        AlertDialog.Builder(this)
            .setTitle(R.string.unknown_title)
            .setMessage(getString(R.string.unknown_code, code))
            .setView(container)
            .setPositiveButton(R.string.unknown_add) { _, _ ->
                val name = nameField.text.toString().trim()
                val cents = priceField.text.toString().trim().toLongOrNull()
                if (name.isEmpty() || cents == null) {
                    Toast.makeText(this, R.string.unknown_name_hint, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                controller.registerUnknownAndAdd(code, name, Money(cents))
            }
            .setNegativeButton(R.string.unknown_cancel, null)
            .show()
    }

    private fun finishSale() {
        val sale = controller.completeSale()
        if (sale == null) {
            Toast.makeText(this, R.string.checkout_empty, Toast.LENGTH_SHORT).show()
            return
        }
        outcomeStatus.text = getString(
            R.string.checkout_sale_done,
            sale.id.take(8),
            sale.total.formatBRL(),
        )
        renderCart()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
