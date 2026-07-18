package com.example.recourse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recourse.R
import com.example.recourse.data.model.Policy

class PolicyAdapter(private val onClick: (Policy) -> Unit) : ListAdapter<Policy, PolicyAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_policy, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(view: View, private val onClick: (Policy) -> Unit) : RecyclerView.ViewHolder(view) {
        private val numberText: TextView = view.findViewById(R.id.policyNumber)
        private val limitText: TextView = view.findViewById(R.id.policyLimit)
        private val statusText: TextView = view.findViewById(R.id.policyStatus)
        private val datesText: TextView = view.findViewById(R.id.policyDates)
        private var currentPolicy: Policy? = null

        init {
            view.setOnClickListener {
                currentPolicy?.let { onClick(it) }
            }
        }

        fun bind(policy: Policy) {
            val context = itemView.context
            currentPolicy = policy
            numberText.text = policy.policyNumber
            limitText.text = context.getString(R.string.coverage_limit_format, policy.coverageLimit, policy.currency)
            statusText.text = policy.status.name
            
            val statusColor = when(policy.status.name) {
                "ACTIVE" -> 0xFF388E3C.toInt()
                "PENDING" -> 0xFFF57C00.toInt()
                else -> 0xFF616161.toInt()
            }
            statusText.background.setTint(statusColor)

            datesText.text = context.getString(R.string.policy_dates_format, policy.effectiveDate, policy.expirationDate)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Policy>() {
        override fun areItemsTheSame(oldItem: Policy, newItem: Policy): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Policy, newItem: Policy): Boolean = oldItem == newItem
    }
}
