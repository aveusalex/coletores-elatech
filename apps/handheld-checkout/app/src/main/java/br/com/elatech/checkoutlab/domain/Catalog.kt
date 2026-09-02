package br.com.elatech.checkoutlab.domain

/** Fonte de produtos. A Fase 4 troca a implementação em memória por Room. */
interface Catalog {
    fun findBySku(sku: String): Product?
    fun all(): List<Product>
    /** Cadastro local de produto fictício (fluxo "código desconhecido → cadastrar"). */
    fun upsert(product: Product)
}

/** Catálogo em memória com massa fictícia. Usado em testes; produção usa Room. */
class InMemoryCatalog(seed: List<Product> = CatalogSeed.PRODUCTS) : Catalog {
    private val bySku = LinkedHashMap<String, Product>().apply {
        seed.forEach { put(it.sku, it) }
    }

    override fun findBySku(sku: String): Product? = bySku[sku]
    override fun all(): List<Product> = bySku.values.toList()
    override fun upsert(product: Product) {
        bySku[product.sku] = product
    }
}
