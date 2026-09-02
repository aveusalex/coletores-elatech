package br.com.elatech.checkoutlab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.elatech.checkoutlab.domain.CompletedSale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Vendas do histórico, com linhas expansíveis. */
class SalesAdapter(private val sales: List<CompletedSale>) :
    RecyclerView.Adapter<SalesAdapter.VH>() {

    private val expanded = HashSet<String>()
    private val fmt = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "BR"))

    override fun getItemCount() = sales.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_sale, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(sales[position])

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val id: TextView = v.findViewById(R.id.saleId)
        private val meta: TextView = v.findViewById(R.id.saleMeta)
        private val total: TextView = v.findViewById(R.id.saleTotal)
        private val chevron: ImageView = v.findViewById(R.id.saleChevron)
        private val header: View = v.findViewById(R.id.saleHeader)
        private val lines: LinearLayout = v.findViewById(R.id.saleLines)

        fun bind(sale: CompletedSale) {
            val ctx = itemView.context
            id.text = sale.id.take(8).uppercase()
            meta.text = ctx.getString(
                R.string.history_row_meta, fmt.format(Date(sale.completedAtEpochMs)), sale.itemCount,
            )
            total.text = sale.total.formatBRL()

            fun applyExpanded(isOpen: Boolean) {
                lines.visibility = if (isOpen) View.VISIBLE else View.GONE
                chevron.setImageResource(if (isOpen) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down)
            }
            applyExpanded(sale.id in expanded)

            lines.removeAllViews()
            sale.lines.forEach { line ->
                val row = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, dp(6), 0, dp(6))
                }
                val name = TextView(ctx).apply {
                    text = line.product.name
                    textSize = 15f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                val qtyPrice = TextView(ctx).apply {
                    text = ctx.getString(R.string.history_line, line.quantity, line.product.price.formatBRL())
                    textSize = 15f
                    setTextColor(ctx.getColor(R.color.on_surface_variant))
                }
                row.addView(name)
                row.addView(qtyPrice)
                lines.addView(row)
            }

            header.setOnClickListener {
                if (sale.id in expanded) expanded.remove(sale.id) else expanded.add(sale.id)
                applyExpanded(sale.id in expanded)
            }
        }

        private fun dp(v: Int) = (v * itemView.resources.displayMetrics.density).toInt()
    }
}
