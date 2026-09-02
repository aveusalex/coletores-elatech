package br.com.elatech.checkoutlab

import br.com.elatech.checkoutlab.domain.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MoneyTest {

    @Test fun format_pt_br() {
        assertEquals("R$ 0,00", Money.ZERO.formatBRL())
        assertEquals("R$ 0,05", Money(5).formatBRL())
        assertEquals("R$ 0,90", Money(90).formatBRL())
        assertEquals("R$ 2,50", Money.ofReais(2, 50).formatBRL())
        assertEquals("R$ 1000,00", Money.ofReais(1000).formatBRL())
    }

    @Test fun arithmetic_stays_in_cents() {
        assertEquals(250L, (Money(100) + Money(150)).cents)
        assertEquals(750L, (Money(250) * 3).cents)
        assertEquals(0L, (Money(500) * 0).cents)
    }

    @Test fun negative_is_rejected() {
        assertThrows(IllegalArgumentException::class.java) { Money(-1) }
        assertThrows(IllegalArgumentException::class.java) { Money(100) * -1 }
    }

    @Test fun of_reais_composes_cents() {
        assertEquals(1890L, Money.ofReais(18, 90).cents)
        assertEquals(500L, Money.ofReais(5).cents)
    }
}
