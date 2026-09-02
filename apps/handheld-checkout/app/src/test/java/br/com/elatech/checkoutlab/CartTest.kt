package br.com.elatech.checkoutlab

import br.com.elatech.checkoutlab.domain.Cart
import br.com.elatech.checkoutlab.domain.Money
import br.com.elatech.checkoutlab.domain.Product
import br.com.elatech.checkoutlab.domain.ScanOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CartTest {
    private val water = Product("111", "Água", Money.ofReais(2, 50))
    private val coffee = Product("222", "Café", Money.ofReais(18, 90))

    @Test fun add_then_add_same_increments_one_line() {
        val cart = Cart()
        assertTrue(cart.add(water) is ScanOutcome.Added)
        assertTrue(cart.add(water) is ScanOutcome.Incremented)
        assertEquals(1, cart.items.size)
        assertEquals(2, cart.itemCount)
        assertEquals(500L, cart.total.cents)
    }

    @Test fun distinct_products_keep_insertion_order() {
        val cart = Cart()
        cart.add(coffee)
        cart.add(water)
        assertEquals(listOf("222", "111"), cart.items.map { it.product.sku })
        assertEquals(2140L, cart.total.cents)
    }

    @Test fun set_quantity_updates_and_zero_removes() {
        val cart = Cart()
        cart.add(water)
        cart.setQuantity("111", 4)
        assertEquals(1000L, cart.total.cents)
        cart.setQuantity("111", 0)
        assertTrue(cart.isEmpty)
    }

    @Test fun set_quantity_negative_throws() {
        val cart = Cart()
        cart.add(water)
        assertThrows(IllegalArgumentException::class.java) { cart.setQuantity("111", -1) }
    }

    @Test fun set_quantity_unknown_sku_is_noop() {
        val cart = Cart()
        cart.add(water)
        cart.setQuantity("999", 3)
        assertEquals(1, cart.itemCount)
    }

    @Test fun clear_empties() {
        val cart = Cart()
        cart.add(water); cart.add(coffee)
        cart.clear()
        assertTrue(cart.isEmpty)
        assertEquals(0L, cart.total.cents)
        assertFalse(cart.items.isNotEmpty())
    }
}
