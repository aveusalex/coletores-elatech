package br.com.elatech.checkoutlab.data

import br.com.elatech.checkoutlab.domain.Catalog
import br.com.elatech.checkoutlab.domain.CartLine
import br.com.elatech.checkoutlab.domain.CompletedSale
import br.com.elatech.checkoutlab.domain.Money
import br.com.elatech.checkoutlab.domain.Product
import br.com.elatech.checkoutlab.domain.SaleHistory

/** [Catalog] sobre Room. Mantém a interface síncrona do domínio. */
class RoomCatalog(private val dao: ProductDao) : Catalog {
    override fun findBySku(sku: String): Product? = dao.findBySku(sku)?.toDomain()
    override fun all(): List<Product> = dao.all().map { it.toDomain() }
    override fun upsert(product: Product) =
        dao.upsert(ProductEntity(product.sku, product.name, product.price.cents))
}

/** [SaleHistory] sobre Room. */
class RoomSaleHistory(private val dao: SaleDao) : SaleHistory {
    override fun record(sale: CompletedSale) {
        dao.record(
            SaleEntity(sale.id, sale.completedAtEpochMs, sale.total.cents, sale.itemCount),
            sale.lines.map {
                SaleLineEntity(
                    saleId = sale.id,
                    sku = it.product.sku,
                    name = it.product.name,
                    priceCents = it.product.price.cents,
                    quantity = it.quantity,
                )
            },
        )
    }

    override fun all(): List<CompletedSale> = dao.allSales().map { sale ->
        val lines = dao.linesOf(sale.id).map {
            CartLine(Product(it.sku, it.name, Money(it.priceCents)), it.quantity)
        }
        CompletedSale(sale.id, sale.completedAtEpochMs, lines, Money(sale.totalCents), sale.itemCount)
    }
}

private fun ProductEntity.toDomain() = Product(sku, name, Money(priceCents))
