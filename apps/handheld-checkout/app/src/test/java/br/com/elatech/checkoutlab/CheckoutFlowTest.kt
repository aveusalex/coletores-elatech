package br.com.elatech.checkoutlab

import android.content.Context
import br.com.elatech.checkoutlab.checkout.CheckoutController
import br.com.elatech.checkoutlab.domain.InMemoryCatalog
import br.com.elatech.checkoutlab.domain.InMemorySaleHistory
import br.com.elatech.checkoutlab.domain.Money
import br.com.elatech.checkoutlab.domain.Product
import br.com.elatech.checkoutlab.domain.ScanOutcome
import br.com.elatech.checkoutlab.scanner.ScanListener
import br.com.elatech.checkoutlab.scanner.ScannerConfig
import br.com.elatech.checkoutlab.scanner.ScannerSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class NoopScannerSource : ScannerSource {
    override fun start(context: Context) = Unit
    override fun stop(context: Context) = Unit
    override fun setListener(listener: ScanListener?) = Unit
    override fun applyConfig(config: ScannerConfig): Boolean = false
    override fun serviceInfo(): String? = null
}

class CheckoutFlowTest {

    private val seed = listOf(
        Product("111", "Água", Money.ofReais(2, 50)),
        Product("222", "Café", Money.ofReais(18, 90)),
    )

    private var clock = 0L
    private val history = InMemorySaleHistory()
    private val controller = CheckoutController(
        scanner = NoopScannerSource(),
        catalog = InMemoryCatalog(seed),
        history = history,
        nowMs = { clock },
    )

    @Test fun known_scan_adds_then_increments_with_correct_total() {
        val a = controller.onScanFor("111")
        assertTrue(a is ScanOutcome.Added)

        clock += 1000
        val b = controller.onScanFor("111")
        assertTrue(b is ScanOutcome.Incremented)
        assertEquals(2, (b as ScanOutcome.Incremented).line.quantity)

        clock += 1000
        controller.onScan("222")
        // 2 * 2,50 + 1 * 18,90 = 23,90
        assertEquals(2390L, controller.cart.total.cents)
        assertEquals(3, controller.cart.itemCount)
    }

    @Test fun repeated_scan_within_debounce_is_ignored() {
        controller.onScan("111")
        clock += 100 // < 400ms
        controller.onScan("111")
        assertEquals(1, controller.cart.itemCount)

        clock += 500 // > 400ms
        controller.onScan("111")
        assertEquals(2, controller.cart.itemCount)
    }

    @Test fun unknown_scan_reports_unknown_and_does_not_touch_cart() {
        val outcome = controller.onScanFor("999")
        assertTrue(outcome is ScanOutcome.Unknown)
        assertEquals("999", (outcome as ScanOutcome.Unknown).code)
        assertTrue(controller.cart.isEmpty)
    }

    @Test fun register_unknown_then_add_puts_it_in_cart() {
        controller.onScan("999")
        controller.registerUnknownAndAdd("999", "Chiclete", Money(175))
        assertEquals(1, controller.cart.itemCount)
        assertEquals(175L, controller.cart.total.cents)
    }

    @Test fun complete_sale_records_history_and_clears_cart() {
        controller.onScan("111")
        clock += 1000
        controller.onScan("222")
        clock = 1_700_000_000_000

        val sale = controller.completeSale()!!
        assertEquals(2140L, sale.total.cents) // 2,50 + 18,90
        assertEquals(2, sale.itemCount)
        assertEquals(1_700_000_000_000, sale.completedAtEpochMs)
        assertTrue(controller.cart.isEmpty)
        assertEquals(1, history.all().size)
    }

    @Test fun complete_sale_on_empty_cart_returns_null() {
        assertNull(controller.completeSale())
    }

    @Test fun set_quantity_zero_removes_line() {
        controller.onScan("111")
        controller.setQuantity("111", 0)
        assertTrue(controller.cart.isEmpty)
    }

    /** Captura o outcome do callback, já que [CheckoutController.onScan] é `void`. */
    private fun CheckoutController.onScanFor(code: String): ScanOutcome {
        var captured: ScanOutcome? = null
        val previous = onOutcome
        onOutcome = { captured = it }
        onScan(code)
        onOutcome = previous
        return captured ?: error("nenhum outcome emitido para $code")
    }
}
