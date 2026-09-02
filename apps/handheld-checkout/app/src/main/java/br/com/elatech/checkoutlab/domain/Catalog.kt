package br.com.elatech.checkoutlab.domain

/** Fonte de produtos. A Fase 4 troca a implementação em memória por Room. */
interface Catalog {
    fun findBySku(sku: String): Product?
    fun all(): List<Product>
    /** Cadastro local de produto fictício (fluxo "código desconhecido → cadastrar"). */
    fun upsert(product: Product)
}

/** Catálogo em memória com massa fictícia. Sem rede, sem dados reais. */
class InMemoryCatalog(seed: List<Product> = DEFAULT_SEED) : Catalog {
    private val bySku = LinkedHashMap<String, Product>().apply {
        seed.forEach { put(it.sku, it) }
    }

    override fun findBySku(sku: String): Product? = bySku[sku]
    override fun all(): List<Product> = bySku.values.toList()
    override fun upsert(product: Product) {
        bySku[product.sku] = product
    }

    companion object {
        /** Códigos fictícios; os EAN-13 usados nos testes de bip do laboratório. */
        val DEFAULT_SEED: List<Product> = listOf(
            Product("7896445490550", "Água mineral 500ml", Money.ofReais(2, 50)),
            Product("7899916918645", "Café torrado 250g", Money.ofReais(18, 90)),
            Product("7891234567895", "Pão de forma", Money.ofReais(9, 90)),
            Product("7890000000012", "Leite integral 1L", Money.ofReais(5, 49)),
            Product("7890000000029", "Barra de cereal", Money.ofReais(3, 25)),
        )
    }
}
