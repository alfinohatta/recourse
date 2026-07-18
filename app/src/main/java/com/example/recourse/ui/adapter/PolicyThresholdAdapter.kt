package com.example.recourse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recourse.R
import com.example.recourse.data.model.PolicyThreshold

class PolicyThresholdAdapter : ListAdapter<PolicyThreshold, PolicyThresholdAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_policy_threshold, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val typeText: TextView = view.findViewById(R.id.triggerType)
        private val valueText: TextView = view.findViewById(R.id.thresholdValue)
        private val descText: TextView = view.findViewById(R.id.thresholdDescription)
        private val logicText: TextView = view.findViewById(R.id.transparentLogicCode)

        fun bind(threshold: PolicyThreshold) {
            val context = itemView.context
            typeText.text = threshold.triggerType.name
            valueText.text = context.getString(R.string.threshold_format, threshold.thresholdValue, threshold.thresholdUnit.name)
            descText.text = threshold.description
            
            // Ref: PROJECT.MD Section 4 "Transparent set of rules"
            val logic = when(threshold.thresholdUnit.name) {
                "CURRENCY" -> context.getString(R.string.logic_currency_format, threshold.thresholdValue, threshold.triggerType.name)
                "PERCENTAGE" -> context.getString(R.string.logic_percentage_format, threshold.thresholdValue.toDouble() / 100)
                else -> context.getString(R.string.logic_count_format, threshold.thresholdValue)
            }
            logicText.text = context.getString(R.string.rules_engine_logic_format, logic)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PolicyThreshold>() {
        override fun areItemsTheSame(oldItem: PolicyThreshold, newItem: PolicyThreshold): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PolicyThreshold, newItem: PolicyThreshold): Boolean = oldItem == newItem
    }
}
