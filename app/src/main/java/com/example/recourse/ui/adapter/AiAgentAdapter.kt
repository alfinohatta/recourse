package com.example.recourse.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recourse.R
import com.example.recourse.data.model.AiAgent

class AiAgentAdapter(private val onClick: (AiAgent) -> Unit) : ListAdapter<AiAgent, AiAgentAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ai_agent, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(view: View, private val onClick: (AiAgent) -> Unit) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.agentName)
        private val typeText: TextView = view.findViewById(R.id.agentType)
        private val statusText: TextView = view.findViewById(R.id.agentStatus)
        private val scoreText: TextView = view.findViewById(R.id.agentScore)
        private val progressBar: com.google.android.material.progressindicator.LinearProgressIndicator = view.findViewById(R.id.agentProgressBar)
        private var currentAgent: AiAgent? = null

        init {
            view.setOnClickListener {
                currentAgent?.let { onClick(it) }
            }
        }

        fun bind(agent: AiAgent) {
            currentAgent = agent
            nameText.text = agent.name
            typeText.text = agent.agentType.name
            statusText.text = agent.status.name
            
            // Status pill color
            val statusColor = when(agent.status.name) {
                "ACTIVE" -> 0xFF388E3C.toInt()
                "PAUSED" -> 0xFFF57C00.toInt()
                else -> 0xFF616161.toInt()
            }
            statusText.background.setTint(statusColor)

            val score = agent.guardrailScore ?: 0.0
            scoreText.text = itemView.context.getString(R.string.percent_format, score.toInt())
            progressBar.progress = score.toInt()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AiAgent>() {
        override fun areItemsTheSame(oldItem: AiAgent, newItem: AiAgent): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AiAgent, newItem: AiAgent): Boolean = oldItem == newItem
    }
}
