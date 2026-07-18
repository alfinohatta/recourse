package com.example.recourse.data.model

import java.math.BigDecimal
import java.time.LocalDateTime

enum class RegulatoryRegion { EU_UK, US, SINGAPORE_ASEAN, UAE_GCC, OTHER }
enum class UserRole { COO_CFO, HEAD_OF_AI, GENERAL_COUNSEL, BROKER, ADMIN }
enum class SubscriptionTier { ASSESS, MONITOR, INSURED, ENTERPRISE }
enum class SubscriptionStatus { PENDING, ACTIVE, CANCELLED, EXPIRED }
enum class AgentType { REFUND, PRICING, MESSAGING, ACCOUNT_MANAGEMENT, BILLING, OTHER }
enum class AutonomyLevel { LOW, MEDIUM, HIGH, FULL }
enum class AgentStatus { ACTIVE, PAUSED, DECOMMISSIONED }
enum class EventType { REFUND_ISSUED, PRICE_CHANGE, MESSAGE_SENT, ACCOUNT_MODIFIED }
enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }
enum class IncidentStatus { FLAGGED, INVESTIGATING, CONFIRMED, RESOLVED, FALSE_POSITIVE }
enum class PolicyStatus { PENDING, ACTIVE, EXPIRED, CANCELLED }
enum class ThresholdUnit { CURRENCY, PERCENTAGE, COUNT }
enum class ClaimStatus { OPENED, UNDER_REVIEW, APPROVED, PAID, DENIED }

data class Company(
    val id: Long,
    val name: String,
    val industry: String,
    val countryCode: String,
    val regulatoryRegion: RegulatoryRegion,
    val websiteUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class User(
    val id: Long,
    val companyId: Long,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class Subscription(
    val id: Long,
    val companyId: Long,
    val tier: SubscriptionTier,
    val status: SubscriptionStatus,
    val monthlyFee: BigDecimal?,
    val oneTimeFee: BigDecimal?,
    val startDate: String,
    val endDate: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AiAgent(
    val id: Long,
    val companyId: Long,
    val name: String,
    val agentType: AgentType,
    val autonomyLevel: AutonomyLevel,
    val status: AgentStatus,
    val guardrailScore: Double?,
    val connectedPlatform: String?, // Mocked for platform demo (Ref: Section 13)
    val connectedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AgentEvent(
    val id: Long,
    val agentId: Long,
    val eventType: EventType,
    val amount: BigDecimal?,
    val currency: String?,
    val isAnomalous: Boolean,
    val metadata: String?, // JSON string
    val occurredAt: LocalDateTime,
    val createdAt: LocalDateTime
)

data class Incident(
    val id: Long,
    val agentId: Long,
    val triggeringEventId: Long?,
    val severity: Severity,
    val status: IncidentStatus,
    val description: String,
    val financialImpact: BigDecimal?,
    val currency: String?,
    val detectedAt: LocalDateTime,
    val resolvedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class Policy(
    val id: Long,
    val companyId: Long,
    val policyNumber: String,
    val coverageLimit: BigDecimal,
    val premiumAnnual: BigDecimal,
    val currency: String,
    val status: PolicyStatus,
    val effectiveDate: String,
    val expirationDate: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class PolicyThreshold(
    val id: Long,
    val policyId: Long,
    val triggerType: EventType,
    val thresholdValue: BigDecimal,
    val thresholdUnit: ThresholdUnit,
    val description: String,
    val createdAt: LocalDateTime
)

data class Claim(
    val id: Long,
    val policyId: Long,
    val incidentId: Long,
    val claimNumber: String,
    val status: ClaimStatus,
    val amountClaimed: BigDecimal,
    val amountPaid: BigDecimal?,
    val currency: String,
    val openedAt: LocalDateTime,
    val resolvedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
