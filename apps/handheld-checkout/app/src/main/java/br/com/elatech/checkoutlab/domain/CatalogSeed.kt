package br.com.elatech.checkoutlab.domain

/**
 * Massa fictícia do laboratório. Nomes e preços são **inventados**; os `sku` são
 * os EAN-13 usados nos testes de bip. Nenhuma consulta a base real.
 */
object CatalogSeed {
    val PRODUCTS: List<Product> = listOf(
        Product("7896445490550", "Água mineral 500ml", Money.ofReais(2, 50)),
        Product("7899916918645", "Café torrado 250g", Money.ofReais(18, 90)),
        Product("7891234567895", "Pão de forma", Money.ofReais(9, 90)),
        Product("7890000000012", "Leite integral 1L", Money.ofReais(5, 49)),
        Product("7890000000029", "Barra de cereal", Money.ofReais(3, 25)),
    )
}
