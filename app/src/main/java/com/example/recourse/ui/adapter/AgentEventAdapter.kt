package com.example.recourse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recourse.R
import com.example.recourse.data.model.AgentEvent
import java.time.format.DateTimeFormatter

class AgentEventAdapter : ListAdapter<AgentEvent, AgentEventAdapter.ViewHolder>(DiffCallback()) {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_agent_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), timeFormatter)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val typeText: TextView = view.findViewById(R.id.eventType)
        private val timeText: TextView = view.findViewById(R.id.eventTime)
        private val amountText: TextView = view.findViewById(R.id.eventAmount)
        private val anomalyText: TextView = view.findViewById(R.id.eventAnomaly)
        private val metadataText: TextView = view.findViewById(R.id.eventMetadata)

        fun bind(event: AgentEvent, formatter: DateTimeFormatter) {
            val context = itemView.context
            typeText.text = event.eventType.name
            timeText.text = event.occurredAt.format(formatter)
            
            if (event.amount != null) {
                amountText.isVisible = true
                amountText.text = context.getString(R.string.incident_financial_format, event.amount, event.currency ?: context.getString(R.string.default_currency))
            } else {
                amountText.isVisible = false
            }

            anomalyText.isVisible = event.isAnomalous
            metadataText.text = event.metadata ?: "{}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AgentEvent>() {
        override fun areItemsTheSame(oldItem: AgentEvent, newItem: AgentEvent): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AgentEvent, newItem: AgentEvent): Boolean = oldItem == newItem
    }
}
