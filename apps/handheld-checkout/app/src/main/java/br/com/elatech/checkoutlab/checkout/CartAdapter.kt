package br.com.elatech.checkoutlab.checkout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import br.com.elatech.checkoutlab.R
import br.com.elatech.checkoutlab.domain.CartLine

/** Lista do carrinho. Ações voltam pelo SKU. */
class CartAdapter(
    private val onInc: (String) -> Unit,
    private val onDec: (String) -> Unit,
    private val onRemove: (String) -> Unit,
) : ListAdapter<CartLine, CartAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_line, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val name: TextView = v.findViewById(R.id.lineName)
        private val sub: TextView = v.findViewById(R.id.lineSub)
        private val qty: TextView = v.findViewById(R.id.lineQty)
        private val dec: ImageButton = v.findViewById(R.id.decButton)
        private val inc: ImageButton = v.findViewById(R.id.incButton)
        private val remove: MaterialButton = v.findViewById(R.id.removeButton)

        fun bind(line: CartLine) {
            val ctx = itemView.context
            name.text = line.product.name
            sub.text = ctx.getString(
                R.string.checkout_line_qty, line.quantity, line.lineTotal.formatBRL(),
            )
            qty.text = line.quantity.toString()
            val sku = line.product.sku
            dec.setOnClickListener { onDec(sku) }
            inc.setOnClickListener { onInc(sku) }
            remove.setOnClickListener { onRemove(sku) }
            dec.contentDescription = ctx.getString(R.string.checkout_dec, line.product.name)
            inc.contentDescription = ctx.getString(R.string.checkout_inc, line.product.name)
            remove.contentDescription = ctx.getString(R.string.checkout_remove_cd, line.product.name)
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<CartLine>() {
            override fun areItemsTheSame(a: CartLine, b: CartLine) = a.product.sku == b.product.sku
            override fun areContentsTheSame(a: CartLine, b: CartLine) = a == b
        }
    }
}
