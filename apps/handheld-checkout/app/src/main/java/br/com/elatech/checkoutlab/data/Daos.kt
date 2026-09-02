package br.com.elatech.checkoutlab.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    fun findBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY name")
    fun all(): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(products: List<ProductEntity>)
}

@Dao
interface SaleDao {
    @Insert
    fun insertSale(sale: SaleEntity)

    @Insert
    fun insertLines(lines: List<SaleLineEntity>)

    @Transaction
    fun record(sale: SaleEntity, lines: List<SaleLineEntity>) {
        insertSale(sale)
        insertLines(lines)
    }

    @Query("SELECT * FROM sales ORDER BY completedAtEpochMs")
    fun allSales(): List<SaleEntity>

    @Query("SELECT * FROM sale_lines WHERE saleId = :saleId")
    fun linesOf(saleId: String): List<SaleLineEntity>
}
