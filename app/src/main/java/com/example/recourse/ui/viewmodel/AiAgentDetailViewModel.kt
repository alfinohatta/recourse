package com.example.recourse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recourse.data.model.AgentEvent
import com.example.recourse.data.model.AgentStatus
import com.example.recourse.data.model.Company
import com.example.recourse.data.repository.AgentEventRepository
import com.example.recourse.data.repository.AiAgentRepository
import com.example.recourse.data.repository.CompanyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AiAgentDetailViewModel : ViewModel() {
    private val eventRepository = AgentEventRepository()
    private val agentRepository = AiAgentRepository()
    private val companyRepository = CompanyRepository()
    
    private val _events = MutableStateFlow<List<AgentEvent>>(emptyList())
    val events: StateFlow<List<AgentEvent>> = _events

    private val _company = MutableStateFlow<Company?>(null)
    val company: StateFlow<Company?> = _company

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _updateSuccess = MutableStateFlow<Boolean?>(null)
    val updateSuccess: StateFlow<Boolean?> = _updateSuccess

    private val _insights = MutableStateFlow<String?>(null)
    val insights: StateFlow<String?> = _insights

    private val _thresholdPulse = MutableStateFlow<String?>(null)
    val thresholdPulse: StateFlow<String?> = _thresholdPulse

    private val _autoPauseEnabled = MutableStateFlow(false)
    val autoPauseEnabled: StateFlow<Boolean> = _autoPauseEnabled

    fun loadEvents(agentId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Load company info to know the region for risk report
            val agents = agentRepository.getAllAgents()
            val agent = agents.find { it.id == agentId }
            agent?.companyId?.let {
                _company.value = companyRepository.getCompanyById(it)
            }

            val events = eventRepository.getEventsByAgentId(agentId)
            _events.value = events
            generateInsights(events)
            
            // Generate Threshold Pulse (Ref: PROJECT.MD Section 4, Layer 2)
            if (agent != null && agent.agentType.name == "REFUND") {
                val currentTotal = eventRepository.getMonthlyTotalForAgent(agentId, com.example.recourse.data.model.EventType.REFUND_ISSUED)
                val limit = java.math.BigDecimal("50000.00") // Parametric trigger baseline
                val percent = currentTotal.divide(limit, 2, java.math.RoundingMode.HALF_UP).multiply(java.math.BigDecimal("100"))
                _thresholdPulse.value = "Monthly Refund Pulse: $${currentTotal.toInt()} / $${limit.toInt()} (${percent.toInt()}%% of trigger limit)"
            }

            _isLoading.value = false
        }
    }

    fun generateInsights(events: List<AgentEvent>) {
        val anomalousRefunds = events.filter { it.isAnomalous && it.eventType.name == "REFUND_ISSUED" }
        if (anomalousRefunds.size >= 2) {
            _insights.value = "Pattern: 'Slow-Bleed' risk detected. Trust Score declining."
        } else if (events.any { it.isAnomalous && (it.amount?.toDouble() ?: 0.0) > 100000 }) {
             _insights.value = "ALERT: Massive anomaly detected. Manual audit recommended."
        } else {
            _insights.value = "Telemetry Pulse: Normal behavior detected. Trust Score: Stable."
        }
    }

    fun toggleStatus(agentId: Long, currentStatus: String) {
        viewModelScope.launch {
            val newStatus = if (currentStatus == AgentStatus.ACTIVE.name) {
                AgentStatus.PAUSED
            } else {
                AgentStatus.ACTIVE
            }
            _updateSuccess.value = agentRepository.updateAgentStatus(agentId, newStatus)
        }
    }

    fun setAutoPause(enabled: Boolean) {
        _autoPauseEnabled.value = enabled
    }

    fun simulateAnomaly(agentId: Long, type: com.example.recourse.data.model.AgentType) {
        viewModelScope.launch {
            val amount = if (type.name == "REFUND") java.math.BigDecimal("340000.00") else java.math.BigDecimal("0.00")
            val eventType = when(type.name) {
                "REFUND" -> com.example.recourse.data.model.EventType.REFUND_ISSUED
                "PRICING" -> com.example.recourse.data.model.EventType.PRICE_CHANGE
                else -> com.example.recourse.data.model.EventType.MESSAGE_SENT
            }
            if (eventRepository.insertSimulatedAnomaly(agentId, amount, eventType)) {
                // If auto-pause is enabled and it's a massive refund, kill the agent
                if (_autoPauseEnabled.value && amount > java.math.BigDecimal("100000.00")) {
                    agentRepository.updateAgentStatus(agentId, AgentStatus.PAUSED)
                    _updateSuccess.value = true // Trigger status refresh in UI
                }
                loadEvents(agentId)
            }
        }
    }
}
