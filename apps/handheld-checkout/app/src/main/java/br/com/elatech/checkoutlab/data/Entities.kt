package br.com.elatech.checkoutlab.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val sku: String,
    val name: String,
    val priceCents: Long,
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val completedAtEpochMs: Long,
    val totalCents: Long,
    val itemCount: Int,
)

@Entity(tableName = "sale_lines")
data class SaleLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: String,
    val sku: String,
    val name: String,
    val priceCents: Long,
    val quantity: Int,
)
