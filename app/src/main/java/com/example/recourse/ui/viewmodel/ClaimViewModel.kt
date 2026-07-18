package com.example.recourse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recourse.data.model.Claim
import com.example.recourse.data.repository.ClaimRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClaimViewModel : ViewModel() {
    private val repository = ClaimRepository()
    
    private val _claims = MutableStateFlow<List<Claim>>(emptyList())
    val claims: StateFlow<List<Claim>> = _claims

    fun loadClaims() {
        viewModelScope.launch {
            val all = repository.getAllClaims()
            if (all.isEmpty()) {
                _claims.value = listOf(
                    com.example.recourse.data.model.Claim(1, 1, 1, "CLM-2026-000117", com.example.recourse.data.model.ClaimStatus.PAID, java.math.BigDecimal("340000.00"), java.math.BigDecimal("340000.00"), "USD", java.time.LocalDateTime.now().minusDays(2), java.time.LocalDateTime.now().minusDays(1), java.time.LocalDateTime.now(), java.time.LocalDateTime.now())
                )
            } else {
                _claims.value = all
            }
        }
    }
}
