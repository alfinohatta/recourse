package com.example.recourse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recourse.data.model.PolicyThreshold
import com.example.recourse.data.repository.AiAgentRepository
import com.example.recourse.data.repository.PolicyRepository
import com.example.recourse.data.repository.PolicyThresholdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PolicyThresholdViewModel : ViewModel() {
    private val repository = PolicyThresholdRepository()
    private val agentRepository = AiAgentRepository()
    private val policyRepository = PolicyRepository()
    
    private val _thresholds = MutableStateFlow<List<PolicyThreshold>>(emptyList())
    val thresholds: StateFlow<List<PolicyThreshold>> = _thresholds

    private val _renewalEstimate = MutableStateFlow<BigDecimal?>(null)
    val renewalEstimate: StateFlow<BigDecimal?> = _renewalEstimate

    fun loadThresholds(policyId: Long) {
        viewModelScope.launch {
            _thresholds.value = repository.getThresholdsByPolicyId(policyId)
        }
    }

    fun calculateRenewalEstimate(policyId: Long, companyId: Long) {
        viewModelScope.launch {
            val policy = policyRepository.getAllPolicies().find { it.id == policyId }
            val avgScore = agentRepository.getAverageGuardrailScore(companyId)
            
            if (policy != null) {
                // Logic: 0.5% discount for every 1 point above 50 guardrail score
                val discountPercent = if (avgScore > 50) (avgScore - 50) * 0.005 else 0.0
                val discount = policy.premiumAnnual.multiply(BigDecimal.valueOf(discountPercent))
                _renewalEstimate.value = policy.premiumAnnual.subtract(discount)
            }
        }
    }
}
