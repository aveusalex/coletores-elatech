package br.com.elatech.checkoutlab.domain

/** Venda simulada concluída. Sem pagamento, sem fiscal — só registro local. */
data class CompletedSale(
    val id: String,
    val completedAtEpochMs: Long,
    val lines: List<CartLine>,
    val total: Money,
    val itemCount: Int,
)

/** Histórico de vendas simuladas. Fase 4 troca a implementação em memória por Room. */
interface SaleHistory {
    fun record(sale: CompletedSale)
    fun all(): List<CompletedSale>
}

class InMemorySaleHistory : SaleHistory {
    private val sales = ArrayList<CompletedSale>()
    override fun record(sale: CompletedSale) { sales.add(sale) }
    override fun all(): List<CompletedSale> = sales.toList()
}
