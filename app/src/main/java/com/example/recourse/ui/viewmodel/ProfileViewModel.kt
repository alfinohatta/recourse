package com.example.recourse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recourse.data.model.Company
import com.example.recourse.data.model.Subscription
import com.example.recourse.data.model.User
import com.example.recourse.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val companyRepository = CompanyRepository()
    private val subscriptionRepository = SubscriptionRepository()
    private val claimRepository = ClaimRepository()
    private val policyRepository = PolicyRepository()
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _company = MutableStateFlow<Company?>(null)
    val company: StateFlow<Company?> = _company

    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions

    private val _totalClaimed = MutableStateFlow(BigDecimal.ZERO)
    val totalClaimed: StateFlow<BigDecimal> = _totalClaimed

    private val _totalPaid = MutableStateFlow(BigDecimal.ZERO)
    val totalPaid: StateFlow<BigDecimal> = _totalPaid

    private val _timeSavedDays = MutableStateFlow(0.0)
    val timeSavedDays: StateFlow<Double> = _timeSavedDays

    private val _avgResolutionTime = MutableStateFlow(0.0)
    val avgResolutionTime: StateFlow<Double> = _avgResolutionTime

    private val _connectedAgentsCount = MutableStateFlow(0)
    val connectedAgentsCount: StateFlow<Int> = _connectedAgentsCount

    private val _lossRatio = MutableStateFlow(0.0)
    val lossRatio: StateFlow<Double> = _lossRatio

    private val _riskReduction = MutableStateFlow(0)
    val riskReduction: StateFlow<Int> = _riskReduction

    private val _committedCapacity = MutableStateFlow(BigDecimal("25000000.00"))
    val committedCapacity: StateFlow<BigDecimal> = _committedCapacity

    private val _brokerReferralRate = MutableStateFlow(84) // 84% from brokers
    val brokerReferralRate: StateFlow<Int> = _brokerReferralRate

    private val _purchaseSuccess = MutableStateFlow<Boolean?>(null)
    val purchaseSuccess: StateFlow<Boolean?> = _purchaseSuccess

    fun loadProfile(userId: Long) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                _user.value = user
                user.companyId.let {
                    _company.value = companyRepository.getCompanyById(it)
                    _subscriptions.value = subscriptionRepository.getSubscriptionsByCompanyId(it)
                    val avgTime = IncidentRepository().getAverageResolutionTime(it)
                    _avgResolutionTime.value = avgTime
                    _timeSavedDays.value = if (avgTime > 0) 90.0 - avgTime else 0.0
                    _connectedAgentsCount.value = AiAgentRepository().getAgentCountByCompanyId(it)
                }
            } else {
                // Fallback Mock Data for demo (Ref: Section 19)
                if (userId == 6L) {
                    _user.value = com.example.recourse.data.model.User(6, 1, "Sam Alvarado", "sam.alvarado@brokerpartners.example.com", com.example.recourse.data.model.UserRole.BROKER, true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now())
                } else {
                    _user.value = com.example.recourse.data.model.User(userId, 1, "Dana Whitfield", "dana.whitfield@northwindapparel.example.com", com.example.recourse.data.model.UserRole.COO_CFO, true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now())
                }
                _company.value = com.example.recourse.data.model.Company(1, "Northwind Apparel Co.", "E-commerce / D2C Retail", "US", com.example.recourse.data.model.RegulatoryRegion.US, "https://northwindapparel.example.com", java.time.LocalDateTime.now(), java.time.LocalDateTime.now())
                _subscriptions.value = listOf(
                    com.example.recourse.data.model.Subscription(1, 1, com.example.recourse.data.model.SubscriptionTier.INSURED, com.example.recourse.data.model.SubscriptionStatus.ACTIVE, java.math.BigDecimal("4500.00"), null, "2026-01-15", "2027-01-14", java.time.LocalDateTime.now(), java.time.LocalDateTime.now())
                )
                _avgResolutionTime.value = 4.2
                _timeSavedDays.value = 85.8
                _connectedAgentsCount.value = 5
                _lossRatio.value = 0.12
                _riskReduction.value = 18
            }
            
            // Calculate total claimed
            val allClaims = claimRepository.getAllClaims()
            val finalClaims = if (allClaims.isEmpty()) {
                listOf(com.example.recourse.data.model.Claim(1, 1, 1, "CLM-2026-000117", com.example.recourse.data.model.ClaimStatus.PAID, java.math.BigDecimal("340000.00"), java.math.BigDecimal("340000.00"), "USD", java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), java.time.LocalDateTime.now()))
            } else allClaims

            val totalClaimedVal = finalClaims.map { it.amountClaimed }.fold(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
            _totalClaimed.value = totalClaimedVal
            val totalPaidVal = finalClaims.map { it.amountPaid ?: java.math.BigDecimal.ZERO }.fold(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
            _totalPaid.value = totalPaidVal
        }
    }

    fun purchaseTier(companyId: Long, tier: com.example.recourse.data.model.SubscriptionTier) {
        viewModelScope.launch {
            val fee = when(tier) {
                com.example.recourse.data.model.SubscriptionTier.MONITOR -> BigDecimal("2500.00")
                com.example.recourse.data.model.SubscriptionTier.INSURED -> BigDecimal("5000.00")
                else -> BigDecimal("10000.00")
            }
            _purchaseSuccess.value = subscriptionRepository.purchaseSubscription(companyId, tier, fee)
        }
    }
}
