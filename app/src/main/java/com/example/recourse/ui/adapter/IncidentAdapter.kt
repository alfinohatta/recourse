package com.example.recourse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recourse.R
import com.example.recourse.data.model.Incident
import com.example.recourse.data.model.Severity

class IncidentAdapter(private val onClick: (Incident) -> Unit) : ListAdapter<Incident, IncidentAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incident, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(view: View, private val onClick: (Incident) -> Unit) : RecyclerView.ViewHolder(view) {
        private val severityText: TextView = view.findViewById(R.id.incidentSeverity)
        private val descriptionText: TextView = view.findViewById(R.id.incidentDescription)
        private val financialText: TextView = view.findViewById(R.id.incidentFinancial)
        private val statusText: TextView = view.findViewById(R.id.incidentStatus)
        private var currentIncident: Incident? = null

        init {
            view.setOnClickListener {
                currentIncident?.let { onClick(it) }
            }
        }

        fun bind(incident: Incident) {
            val context = itemView.context
            currentIncident = incident
            severityText.text = incident.severity.name
            val severityColor = when(incident.severity) {
                Severity.CRITICAL -> 0xFFD32F2F.toInt()
                Severity.HIGH -> 0xFFF57C00.toInt()
                Severity.MEDIUM -> 0xFFFBC02D.toInt()
                else -> 0xFF1976D2.toInt()
            }
            severityText.background.setTint(severityColor)

            descriptionText.text = incident.description
            financialText.text = incident.financialImpact?.let { context.getString(R.string.incident_financial_format, it, incident.currency ?: context.getString(R.string.default_currency)) } ?: ""
            statusText.text = incident.status.name
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Incident>() {
        override fun areItemsTheSame(oldItem: Incident, newItem: Incident): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Incident, newItem: Incident): Boolean = oldItem == newItem
    }
}
