package com.example.recourse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recourse.data.model.AiAgent
import com.example.recourse.data.repository.AiAgentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AiAgentViewModel : ViewModel() {
    private val repository = AiAgentRepository()
    
    private val _agents = MutableStateFlow<List<AiAgent>>(emptyList())
    val agents: StateFlow<List<AiAgent>> = _agents

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadAgents() {
        viewModelScope.launch {
            _isLoading.value = true
            val results = repository.getAllAgents()
            if (results.isEmpty()) {
                // Mock data for demo if DB is empty/fails
                _agents.value = listOf(
                    com.example.recourse.data.model.AiAgent(1, 1, "Refund Concierge Bot", com.example.recourse.data.model.AgentType.REFUND, com.example.recourse.data.model.AutonomyLevel.HIGH, com.example.recourse.data.model.AgentStatus.ACTIVE, 72.5, "Stripe / Zendesk", java.time.LocalDateTime.now().minusDays(10), java.time.LocalDateTime.now(), java.time.LocalDateTime.now()),
                    com.example.recourse.data.model.AiAgent(2, 1, "Dynamic Pricing Agent", com.example.recourse.data.model.AgentType.PRICING, com.example.recourse.data.model.AutonomyLevel.MEDIUM, com.example.recourse.data.model.AgentStatus.ACTIVE, 65.0, "Shopify / Magento", java.time.LocalDateTime.now().minusDays(10), java.time.LocalDateTime.now(), java.time.LocalDateTime.now())
                )
            } else {
                _agents.value = results
            }
            _isLoading.value = false
        }
    }
}
