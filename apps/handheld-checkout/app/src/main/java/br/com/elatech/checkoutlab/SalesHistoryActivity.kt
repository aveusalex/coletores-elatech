package br.com.elatech.checkoutlab

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import br.com.elatech.checkoutlab.data.AppDatabase
import br.com.elatech.checkoutlab.data.RoomSaleHistory
import br.com.elatech.checkoutlab.domain.CompletedSale
import java.text.DateFormat
import java.util.Date

/** Lista as vendas simuladas gravadas em Room. Somente leitura. */
class SalesHistoryActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val history = RoomSaleHistory(AppDatabase.get(this).saleDao())
        val sales = history.all().sortedByDescending { it.completedAtEpochMs }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        root.addView(
            TextView(this).apply {
                text = getString(R.string.history_title)
                textSize = 22f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            },
        )

        if (sales.isEmpty()) {
            root.addView(TextView(this).apply { text = getString(R.string.history_empty); textSize = 15f })
        } else {
            sales.forEach { root.addView(saleView(it)) }
        }

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun saleView(sale: CompletedSale) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, dp(14), 0, dp(6))
        addView(
            TextView(this@SalesHistoryActivity).apply {
                text = getString(
                    R.string.history_row,
                    sale.id.take(8),
                    DateFormat.getDateTimeInstance().format(Date(sale.completedAtEpochMs)),
                    sale.itemCount,
                    sale.total.formatBRL(),
                )
                textSize = 15f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            },
        )
        sale.lines.forEach { line ->
            addView(
                TextView(this@SalesHistoryActivity).apply {
                    text = "  ${line.product.name} · ${line.quantity} × ${line.product.price.formatBRL()}"
                    textSize = 13f
                },
            )
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
