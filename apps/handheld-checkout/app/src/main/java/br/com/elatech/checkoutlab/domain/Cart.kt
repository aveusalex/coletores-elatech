package br.com.elatech.checkoutlab.domain

/** Uma linha do carrinho: um produto e a quantidade. */
data class CartLine(
    val product: Product,
    val quantity: Int,
) {
    init {
        require(quantity >= 1) { "linha de carrinho com quantidade < 1" }
    }

    val lineTotal: Money get() = product.price * quantity
}

/** Resultado de aplicar uma leitura ao carrinho. */
sealed interface ScanOutcome {
    data class Added(val line: CartLine) : ScanOutcome
    data class Incremented(val line: CartLine) : ScanOutcome
    data class Unknown(val code: String) : ScanOutcome
}

/**
 * Carrinho em memória. Sem efeitos externos; a persistência (histórico de vendas)
 * é responsabilidade de outra camada.
 */
class Cart {
    private val lines = LinkedHashMap<String, CartLine>()

    val items: List<CartLine> get() = lines.values.toList()
    val total: Money get() = lines.values.fold(Money.ZERO) { acc, l -> acc + l.lineTotal }
    val itemCount: Int get() = lines.values.sumOf { it.quantity }
    val isEmpty: Boolean get() = lines.isEmpty()

    /** Adiciona 1 do produto, ou incrementa se já existe. */
    fun add(product: Product): ScanOutcome {
        val existing = lines[product.sku]
        val updated = if (existing == null) {
            CartLine(product, 1)
        } else {
            existing.copy(quantity = existing.quantity + 1)
        }
        lines[product.sku] = updated
        return if (existing == null) ScanOutcome.Added(updated) else ScanOutcome.Incremented(updated)
    }

    /** Define a quantidade exata; `0` remove a linha. */
    fun setQuantity(sku: String, quantity: Int) {
        require(quantity >= 0) { "quantidade negativa: $quantity" }
        val existing = lines[sku] ?: return
        if (quantity == 0) lines.remove(sku) else lines[sku] = existing.copy(quantity = quantity)
    }

    fun remove(sku: String) {
        lines.remove(sku)
    }

    fun clear() {
        lines.clear()
    }
}
