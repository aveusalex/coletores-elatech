package br.com.elatech.checkoutlab

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.elatech.checkoutlab.data.AppDatabase
import br.com.elatech.checkoutlab.data.RoomSaleHistory
import br.com.elatech.checkoutlab.databinding.ActivitySalesHistoryBinding
import br.com.elatech.checkoutlab.domain.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Histórico de vendas simuladas (Room). Somente leitura. */
class SalesHistoryActivity : AppCompatActivity() {

    private lateinit var b: ActivitySalesHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySalesHistoryBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.subbar.subbarTitle.text = getString(R.string.history_title)
        b.subbar.subbarBack.setOnClickListener { finish() }

        val history = RoomSaleHistory(AppDatabase.get(this).saleDao())
        val sales = history.all().sortedByDescending { it.completedAtEpochMs }

        if (sales.isEmpty()) {
            b.emptyState.visibility = View.VISIBLE
            b.salesList.visibility = View.GONE
            b.summary.visibility = View.GONE
            return
        }

        b.summary.visibility = View.VISIBLE
        b.summaryCount.text = if (sales.size == 1) {
            getString(R.string.history_summary_count_one)
        } else {
            getString(R.string.history_summary_count, sales.size)
        }
        b.summaryTotal.text = Money(sales.sumOf { it.total.cents }).formatBRL()
        b.summaryDate.text = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date())

        b.salesList.layoutManager = LinearLayoutManager(this)
        b.salesList.adapter = SalesAdapter(sales)
    }
}
