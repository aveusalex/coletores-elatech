package br.com.elatech.checkoutlab.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.elatech.checkoutlab.domain.CatalogSeed

@Database(
    entities = [ProductEntity::class, SaleEntity::class, SaleLineEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): AppDatabase {
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "checkoutlab.db",
            )
                // Lab: base trivial (poucos produtos) e sem coroutines. Produção
                // usaria consultas fora da thread principal.
                .allowMainThreadQueries()
                .build()

            if (db.productDao().count() == 0) {
                db.productDao().upsertAll(
                    CatalogSeed.PRODUCTS.map {
                        ProductEntity(it.sku, it.name, it.price.cents)
                    },
                )
            }
            return db
        }
    }
}
