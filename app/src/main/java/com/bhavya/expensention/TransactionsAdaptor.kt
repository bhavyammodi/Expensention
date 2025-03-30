package com.bhavya.expensention

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date

class TransactionsAdaptor(private var transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionsAdaptor.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val label: TextView = itemView.findViewById(R.id.label)
        val amount: TextView = itemView.findViewById(R.id.amount)
        val date: TextView = itemView.findViewById(R.id.date)
        val time: TextView = itemView.findViewById(R.id.time)
        val classification: TextView = itemView.findViewById(R.id.classificationTextView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_layout, parent, false)
        return TransactionViewHolder(view)
    }


    override fun getItemCount(): Int {
        return transactions.size
    }

    private fun convertMillisToDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a")
        val date = Date(millis)
        return (sdf.format(date)).toUpperCase()
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.amount.context

        if(transaction.isExpense){
            holder.amount.text = "- Rs %.2f".format(transaction.amount)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red))
        }
        else{
            holder.amount.text = "+ Rs %.2f".format(transaction.amount)
            holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green))
        }
        holder.label.text = transaction.label
        val dateTime = convertMillisToDateTime(transaction.time)
        val dateTimeParts = dateTime.split(" ")
        holder.date.text = dateTimeParts[0]
        holder.time.text = "${dateTimeParts[1]} ${dateTimeParts[2]}"
        holder.classification.text = transaction.classification
    }

    fun setData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}