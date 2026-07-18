package com.example.recourse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recourse.data.model.Incident
import com.example.recourse.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IncidentViewModel : ViewModel() {
    private val repository = IncidentRepository()
    
    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents

    private val _systemicAlert = MutableStateFlow<String?>(null)
    val systemicAlert: StateFlow<String?> = _systemicAlert

    fun loadIncidents() {
        viewModelScope.launch {
            val all = repository.getAllIncidents()
            if (all.isEmpty()) {
                _incidents.value = listOf(
                    com.example.recourse.data.model.Incident(1, 1, 1, com.example.recourse.data.model.Severity.CRITICAL, com.example.recourse.data.model.IncidentStatus.FLAGGED, "Refund agent misread an edge case and approved a $340,000 refund on a $340 order.", java.math.BigDecimal("340000.00"), "USD", java.time.LocalDateTime.now().minusHours(2), null, java.time.LocalDateTime.now(), java.time.LocalDateTime.now())
                )
            } else {
                _incidents.value = all
            }
            checkSystemicRisk(_incidents.value)
        }
    }

    private fun checkSystemicRisk(incidents: List<Incident>) {
        val activeIncidents = incidents.filter { it.status.name == "FLAGGED" || it.status.name == "CONFIRMED" }
        if (activeIncidents.size >= 3) {
            _systemicAlert.value = "Correlated spike detected across multiple agents. Investigating potential systemic foundation-model failure (Scenario C)."
        } else {
            _systemicAlert.value = null
        }
    }
}
