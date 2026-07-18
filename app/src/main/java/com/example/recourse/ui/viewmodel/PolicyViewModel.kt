package com.example.recourse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recourse.data.model.Policy
import com.example.recourse.data.repository.PolicyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PolicyViewModel : ViewModel() {
    private val repository = PolicyRepository()
    
    private val _policies = MutableStateFlow<List<Policy>>(emptyList())
    val policies: StateFlow<List<Policy>> = _policies

    fun loadPolicies() {
        viewModelScope.launch {
            val all = repository.getAllPolicies()
            if (all.isEmpty()) {
                _policies.value = listOf(
                    com.example.recourse.data.model.Policy(1, 1, "RCA-2026-US-00042", java.math.BigDecimal("5000000.00"), java.math.BigDecimal("118000.00"), "USD", com.example.recourse.data.model.PolicyStatus.ACTIVE, "2026-01-15", "2027-01-14", java.time.LocalDateTime.now(), java.time.LocalDateTime.now())
                )
            } else {
                _policies.value = all
            }
        }
    }
}
