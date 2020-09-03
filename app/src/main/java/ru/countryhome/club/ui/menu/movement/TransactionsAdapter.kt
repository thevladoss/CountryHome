package ru.countryhome.club.ui.menu.movement

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import ru.countryhome.club.MainActivity
import ru.countryhome.club.R
import ru.countryhome.club.ui.detail.DetailActivity
import kotlin.coroutines.coroutineContext


internal class TransactionsAdapter(context: Context, private val transaction: List<Transactions>) :
    RecyclerView.Adapter<TransactionsAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var mClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.item_transactions, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transaction[position]
        holder.img.setImageResource(transaction.image)
        holder.id.text = transaction.id
        holder.date.text = transaction.date
        holder.from.text = transaction.from
        holder.to.text = transaction.to
        holder.cost.text = transaction.cost
    }

    override fun getItemCount(): Int {
        return transaction.size
    }

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        internal val img: ImageView = view.findViewById(R.id.img_transaction)
        internal val id: TextView = view.findViewById(R.id.text_trans_id)
        internal val date: TextView = view.findViewById(R.id.text_trans_date)
        internal val from: TextView = view.findViewById(R.id.text_trans_move_from)
        internal val to: TextView = view.findViewById(R.id.text_trans_move_to)
        internal val cost: TextView = view.findViewById(R.id.text_trans_cost)
        internal val view: LinearLayout = view.findViewById(R.id.layout_item)

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            mClickListener?.onItemClick(view, adapterPosition)
        }
    }

    fun setOnClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

}


class Transactions (var image: Int, var id: String, var date: String, var from: String, var to: String,  var cost: String)

class MovesJson (var id: String, var owner: String, var moment: String, val name: String, var sum: String, var applicable: String, var created: String, var sourceStore: String, var targetStore: String, var nameApply: String, var dateApply: String)

class PositionsJson (var quantity: String, var price: String, var name: String, var img: String)