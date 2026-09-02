package br.com.elatech.checkoutlab.checkout

import android.content.Context
import br.com.elatech.checkoutlab.domain.Cart
import br.com.elatech.checkoutlab.domain.Catalog
import br.com.elatech.checkoutlab.domain.CompletedSale
import br.com.elatech.checkoutlab.domain.Money
import br.com.elatech.checkoutlab.domain.Product
import br.com.elatech.checkoutlab.domain.SaleHistory
import br.com.elatech.checkoutlab.domain.ScanOutcome
import br.com.elatech.checkoutlab.scanner.ScannerConfig
import br.com.elatech.checkoutlab.scanner.ScannerSource
import java.util.UUID

/**
 * Liga [ScannerSource] → [Catalog] → [Cart] → [SaleHistory]. Sem framework de UI:
 * a Activity fornece o `Context`, assina os callbacks e desenha o estado.
 */
class CheckoutController(
    private val scanner: ScannerSource,
    private val catalog: Catalog,
    private val history: SaleHistory,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    val cart = Cart()

    /** Config aplicada ao scanner no próximo [attach]. Reler do store antes de anexar. */
    var scannerConfig: ScannerConfig = ScannerConfig.CHECKOUT_DEFAULT

    /** Chamado a cada leitura processada (na thread do receiver). */
    var onOutcome: ((ScanOutcome) -> Unit)? = null

    /** Chamado quando o conteúdo do carrinho muda. */
    var onCartChanged: (() -> Unit)? = null

    private var lastCode: String? = null
    private var lastCodeAtMs: Long = 0

    fun attach(context: Context) {
        scanner.setListener { event -> onScan(event.value) }
        scanner.start(context)
        // Fontes que controlam o hardware (SDK) aplicam; broadcast ignora e devolve false.
        scanner.applyConfig(scannerConfig)
    }

    fun detach(context: Context) {
        scanner.stop(context)
        scanner.setListener(null)
    }

    /** Processa uma leitura. Ignora repetição idêntica dentro de [DEBOUNCE_MS]. */
    fun onScan(rawCode: String) {
        val code = rawCode.trim()
        if (code.isEmpty()) return

        val now = nowMs()
        if (code == lastCode && now - lastCodeAtMs < DEBOUNCE_MS) return
        lastCode = code
        lastCodeAtMs = now

        val product = catalog.findBySku(code)
        val outcome = if (product == null) ScanOutcome.Unknown(code) else cart.add(product)
        onOutcome?.invoke(outcome)
        if (outcome !is ScanOutcome.Unknown) onCartChanged?.invoke()
    }

    /** Fluxo "código desconhecido → cadastrar produto fictício e adicionar". */
    fun registerUnknownAndAdd(code: String, name: String, price: Money) {
        catalog.upsert(Product(code.trim(), name.trim(), price))
        lastCode = null // permite readicionar já
        onScan(code)
    }

    fun setQuantity(sku: String, quantity: Int) {
        cart.setQuantity(sku, quantity)
        onCartChanged?.invoke()
    }

    fun increment(sku: String) {
        val line = cart.items.find { it.product.sku == sku } ?: return
        setQuantity(sku, line.quantity + 1)
    }

    fun decrement(sku: String) {
        val line = cart.items.find { it.product.sku == sku } ?: return
        setQuantity(sku, line.quantity - 1)
    }

    fun remove(sku: String) {
        cart.remove(sku)
        onCartChanged?.invoke()
    }

    /** Fecha a venda simulada, registra no histórico e limpa o carrinho. */
    fun completeSale(): CompletedSale? {
        if (cart.isEmpty) return null
        val sale = CompletedSale(
            id = UUID.randomUUID().toString(),
            completedAtEpochMs = nowMs(),
            lines = cart.items,
            total = cart.total,
            itemCount = cart.itemCount,
        )
        history.record(sale)
        cart.clear()
        lastCode = null
        onCartChanged?.invoke()
        return sale
    }

    fun completedSales(): List<CompletedSale> = history.all()

    private companion object {
        const val DEBOUNCE_MS = 400L
    }
}
