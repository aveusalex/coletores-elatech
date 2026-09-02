package br.com.elatech.checkoutlab

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import br.com.elatech.checkoutlab.checkout.CartAdapter
import br.com.elatech.checkoutlab.checkout.CheckoutController
import br.com.elatech.checkoutlab.data.AppDatabase
import br.com.elatech.checkoutlab.data.RoomCatalog
import br.com.elatech.checkoutlab.data.RoomSaleHistory
import br.com.elatech.checkoutlab.databinding.ActivityMainBinding
import br.com.elatech.checkoutlab.domain.Money
import br.com.elatech.checkoutlab.domain.ScanOutcome
import br.com.elatech.checkoutlab.scanner.ScannerConfigStore
import br.com.elatech.checkoutlab.scanner.SdkScannerSource

/** Checkout offline. Identidade Elatech (UX v1). */
class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private val controller: CheckoutController by lazy {
        val db = AppDatabase.get(this)
        CheckoutController(
            scanner = SdkScannerSource(),
            catalog = RoomCatalog(db.productDao()),
            history = RoomSaleHistory(db.saleDao()),
        )
    }

    private val adapter = CartAdapter(
        onInc = { controller.increment(it) },
        onDec = { controller.decrement(it) },
        onRemove = { controller.remove(it) },
    )

    private val revertBanner = Runnable { showWaiting() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.cartList.layoutManager = LinearLayoutManager(this)
        b.cartList.adapter = adapter

        b.finishButton.setOnClickListener { finishSale() }
        b.clearButton.setOnClickListener { controller.cart.clear(); renderCart(); showWaiting() }
        b.settingsButton.setOnClickListener { open(ScannerSettingsActivity::class.java) }
        b.historyButton.setOnClickListener { open(SalesHistoryActivity::class.java) }
        b.diagnosticButton.setOnClickListener { open(DiagnosticActivity::class.java) }
        b.moreButton.setOnClickListener { showOverflow() }

        controller.onCartChanged = { runOnUiThread { renderCart() } }
        controller.onOutcome = { outcome -> runOnUiThread { renderOutcome(outcome) } }

        showWaiting()
    }

    override fun onResume() {
        super.onResume()
        controller.scannerConfig = ScannerConfigStore(this).load()
        controller.attach(this)
        renderCart()
    }

    override fun onPause() {
        controller.detach(this)
        b.scanBanner.removeCallbacks(revertBanner)
        super.onPause()
    }

    // ── carrinho ──

    private fun renderCart() {
        val cart = controller.cart
        b.totalValue.text = cart.total.formatBRL()
        b.itemCount.text = if (cart.itemCount == 1) {
            getString(R.string.checkout_items_one)
        } else {
            getString(R.string.checkout_items_many, cart.itemCount)
        }
        b.emptyState.visibility = if (cart.isEmpty) android.view.View.VISIBLE else android.view.View.GONE
        b.cartList.visibility = if (cart.isEmpty) android.view.View.GONE else android.view.View.VISIBLE
        b.finishButton.isEnabled = !cart.isEmpty
        b.clearButton.isEnabled = !cart.isEmpty
        adapter.submitList(cart.items)
    }

    // ── banner ──

    private fun renderOutcome(outcome: ScanOutcome) {
        b.scanBanner.removeCallbacks(revertBanner)
        when (outcome) {
            is ScanOutcome.Added -> banner(
                R.color.banner_added_bg, R.color.banner_added_fg, R.drawable.ic_check_circle,
                getString(R.string.checkout_outcome_added, outcome.line.product.name),
            )
            is ScanOutcome.Incremented -> banner(
                R.color.banner_inc_bg, R.color.banner_inc_fg, R.drawable.ic_plus,
                getString(
                    R.string.checkout_outcome_incremented,
                    outcome.line.product.name, outcome.line.quantity,
                ),
            )
            is ScanOutcome.Unknown -> {
                banner(
                    R.color.banner_unknown_bg, R.color.banner_unknown_fg, R.drawable.ic_alert_triangle,
                    getString(R.string.checkout_outcome_unknown, outcome.code),
                )
                openUnknownSheet(outcome.code)
            }
        }
    }

    private fun banner(bgRes: Int, fgRes: Int, iconRes: Int, text: String) {
        val fg = ContextCompat.getColor(this, fgRes)
        b.scanBanner.setBackgroundColor(ContextCompat.getColor(this, bgRes))
        b.scanBannerIcon.setImageResource(iconRes)
        b.scanBannerIcon.setColorFilter(fg)
        b.scanBannerText.setTextColor(fg)
        b.scanBannerText.text = text
    }

    private fun showWaiting() = banner(
        R.color.banner_wait_bg, R.color.banner_wait_fg, R.drawable.ic_barcode,
        getString(R.string.checkout_outcome_none),
    )

    private fun finishSale() {
        val sale = controller.completeSale() ?: return
        banner(
            R.color.banner_done_bg, R.color.banner_done_fg, R.drawable.ic_check_circle,
            getString(R.string.checkout_sale_done, sale.id.take(8).uppercase(), sale.total.formatBRL()),
        )
        renderCart()
        b.scanBanner.postDelayed(revertBanner, 2500)
    }

    // ── código desconhecido ──

    private fun openUnknownSheet(code: String) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.sheet_unknown_product, null)
        dialog.setContentView(view)

        view.findViewById<android.widget.TextView>(R.id.sheetCode).text = code
        val nameLayout = view.findViewById<TextInputLayout>(R.id.nameLayout)
        val priceLayout = view.findViewById<TextInputLayout>(R.id.priceLayout)
        val nameInput = view.findViewById<TextInputEditText>(R.id.nameInput)
        val priceInput = view.findViewById<TextInputEditText>(R.id.priceInput)

        priceInput.addTextChangedListener(CentsMaskWatcher(priceInput))
        nameInput.addTextChangedListener(clearErrorWatcher(nameLayout))
        priceInput.addTextChangedListener(clearErrorWatcher(priceLayout))

        view.findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener { dialog.dismiss() }
        view.findViewById<MaterialButton>(R.id.submitButton).setOnClickListener {
            val name = nameInput.text?.toString()?.trim().orEmpty()
            val cents = priceInput.text?.toString().orEmpty().filter { it.isDigit() }.toLongOrNull() ?: 0L
            var ok = true
            if (name.isEmpty()) { nameLayout.error = getString(R.string.unknown_err_name); ok = false }
            if (cents <= 0L) { priceLayout.error = getString(R.string.unknown_err_price); ok = false }
            if (!ok) return@setOnClickListener
            controller.registerUnknownAndAdd(code, name, Money(cents))
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun clearErrorWatcher(layout: TextInputLayout) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
        override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { layout.error = null }
        override fun afterTextChanged(s: Editable?) = Unit
    }

    /** Máscara de centavos: só dígitos → "R$ x,xx". */
    private inner class CentsMaskWatcher(private val target: TextInputEditText) : TextWatcher {
        private var editing = false
        override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
        override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) = Unit
        override fun afterTextChanged(s: Editable?) {
            if (editing) return
            editing = true
            val digits = s?.toString().orEmpty().filter { it.isDigit() }.take(8)
            val formatted = if (digits.isEmpty()) "" else Money(digits.toLong()).formatBRL()
            target.setText(formatted)
            target.setSelection(formatted.length)
            editing = false
        }
    }

    // ── navegação ──

    private fun showOverflow() {
        PopupMenu(this, b.moreButton).apply {
            menuInflater.inflate(R.menu.checkout_overflow, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_settings -> open(ScannerSettingsActivity::class.java)
                    R.id.action_history -> open(SalesHistoryActivity::class.java)
                    R.id.action_diagnostic -> open(DiagnosticActivity::class.java)
                }
                true
            }
            show()
        }
    }

    private fun open(cls: Class<*>) = startActivity(Intent(this, cls))
}
