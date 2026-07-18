package com.example.recourse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recourse.data.model.Incident
import com.example.recourse.data.repository.IncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClaimDetailViewModel : ViewModel() {
    private val incidentRepository = IncidentRepository()
    
    private val _linkedIncident = MutableStateFlow<Incident?>(null)
    val linkedIncident: StateFlow<Incident?> = _linkedIncident

    fun loadIncident(incidentId: Long) {
        viewModelScope.launch {
            _linkedIncident.value = incidentRepository.getIncidentById(incidentId)
        }
    }
}
