package br.com.elatech.checkoutlab.domain

/**
 * Produto fictício do laboratório. `sku` é o conteúdo do código de barras lido
 * (EAN-13, QR, etc.) — chave natural do catálogo local.
 */
data class Product(
    val sku: String,
    val name: String,
    val price: Money,
)
