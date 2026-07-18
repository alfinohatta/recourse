package com.example.recourse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recourse.data.model.*
import com.example.recourse.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class IncidentDetailViewModel : ViewModel() {
    private val eventRepository = AgentEventRepository()
    private val incidentRepository = IncidentRepository()
    private val claimRepository = ClaimRepository()
    private val policyRepository = PolicyRepository()
    private val thresholdRepository = PolicyThresholdRepository()
    
    private val _triggerEvent = MutableStateFlow<AgentEvent?>(null)
    val triggerEvent: StateFlow<AgentEvent?> = _triggerEvent

    private val _matchedThreshold = MutableStateFlow<PolicyThreshold?>(null)
    val matchedThreshold: StateFlow<PolicyThreshold?> = _matchedThreshold

    private val _existingClaim = MutableStateFlow<Claim?>(null)
    val existingClaim: StateFlow<Claim?> = _existingClaim

    private val _updateSuccess = MutableStateFlow<Boolean?>(null)
    val updateSuccess: StateFlow<Boolean?> = _updateSuccess

    private val _claimSuccess = MutableStateFlow<Boolean?>(null)
    val claimSuccess: StateFlow<Boolean?> = _claimSuccess

    fun loadTriggerEvent(eventId: Long) {
        viewModelScope.launch {
            val event = eventRepository.getEventById(eventId)
            _triggerEvent.value = event
            
            if (event != null) {
                // Find matching policy threshold
                val policy = policyRepository.getAllPolicies().firstOrNull { it.status.name == "ACTIVE" }
                if (policy != null) {
                    _matchedThreshold.value = thresholdRepository.getThresholdForEvent(policy.id, event.eventType)
                }
            }
        }
    }

    fun checkExistingClaim(incidentId: Long) {
        viewModelScope.launch {
            _existingClaim.value = claimRepository.getClaimByIncidentId(incidentId)
        }
    }

    fun updateStatus(incidentId: Long, status: IncidentStatus) {
        viewModelScope.launch {
            _updateSuccess.value = incidentRepository.updateIncidentStatus(incidentId, status)
        }
    }

    fun openClaim(incidentId: Long, amount: BigDecimal, currency: String) {
        viewModelScope.launch {
            // In a real app, we'd know the company/policy. 
            // For prototype, we fetch the first active policy of the company.
            val policy = policyRepository.getAllPolicies().firstOrNull { it.status.name == "ACTIVE" }
            if (policy != null) {
                _claimSuccess.value = claimRepository.createClaim(policy.id, incidentId, amount, currency)
            } else {
                _claimSuccess.value = false
            }
        }
    }
}
