package br.com.elatech.checkoutlab.domain

import java.util.Locale

/**
 * Dinheiro em **centavos inteiros** (ADR 0001). Sem ponto flutuante em preço.
 * `cents` é sempre >= 0 neste laboratório (sem estorno/desconto negativo).
 */
@JvmInline
value class Money(val cents: Long) {
    init {
        require(cents >= 0) { "Money não pode ser negativo: $cents" }
    }

    operator fun plus(other: Money) = Money(cents + other.cents)
    operator fun times(quantity: Int): Money {
        require(quantity >= 0) { "quantidade negativa: $quantity" }
        return Money(cents * quantity)
    }

    /** Ex.: `R$ 12,90`. Formatação simples pt-BR, sem depender de `NumberFormat` de moeda. */
    fun formatBRL(): String {
        val reais = cents / 100
        val centavos = cents % 100
        return String.format(Locale("pt", "BR"), "R$ %d,%02d", reais, centavos)
    }

    companion object {
        val ZERO = Money(0)
        fun ofReais(reais: Long, centavos: Long = 0): Money = Money(reais * 100 + centavos)
    }
}
