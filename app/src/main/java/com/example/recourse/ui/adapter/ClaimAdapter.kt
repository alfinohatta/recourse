package com.example.recourse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recourse.R
import com.example.recourse.data.model.Claim
import java.time.format.DateTimeFormatter

class ClaimAdapter(private val onClick: (Claim) -> Unit) : ListAdapter<Claim, ClaimAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_claim, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), dateFormatter)
    }

    class ViewHolder(view: View, private val onClick: (Claim) -> Unit) : RecyclerView.ViewHolder(view) {
        private val numberText: TextView = view.findViewById(R.id.claimNumber)
        private val amountText: TextView = view.findViewById(R.id.claimAmount)
        private val statusText: TextView = view.findViewById(R.id.claimStatus)
        private val paidText: TextView = view.findViewById(R.id.claimPaid)
        private var currentClaim: Claim? = null

        init {
            view.setOnClickListener {
                currentClaim?.let { onClick(it) }
            }
        }

        fun bind(claim: Claim, formatter: DateTimeFormatter) {
            val context = itemView.context
            currentClaim = claim
            numberText.text = claim.claimNumber
            amountText.text = context.getString(R.string.incident_financial_format, claim.amountClaimed, claim.currency)
            statusText.text = claim.status.name
            
            val statusColor = when(claim.status.name) {
                "PAID" -> 0xFF388E3C.toInt()
                "APPROVED" -> 0xFF00BFA5.toInt()
                "DENIED" -> 0xFFD32F2F.toInt()
                else -> 0xFFF57C00.toInt()
            }
            statusText.background.setTint(statusColor)

            paidText.text = if (claim.amountPaid != null) {
                context.getString(R.string.paid_amount_format, claim.amountPaid, claim.currency)
            } else {
                context.getString(R.string.in_review_msg)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Claim>() {
        override fun areItemsTheSame(oldItem: Claim, newItem: Claim): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Claim, newItem: Claim): Boolean = oldItem == newItem
    }
}
