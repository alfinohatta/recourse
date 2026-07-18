package com.example.recourse

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.recourse.ui.activity.*
import com.example.recourse.ui.adapter.*
import com.example.recourse.ui.viewmodel.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import com.example.recourse.R

class MainActivity : AppCompatActivity() {

    private val agentViewModel: AiAgentViewModel by viewModels()
    private val incidentViewModel: IncidentViewModel by viewModels()
    private val policyViewModel: PolicyViewModel by viewModels()
    private val claimViewModel: ClaimViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    
    private lateinit var agentAdapter: AiAgentAdapter
    private lateinit var incidentAdapter: IncidentAdapter
    private lateinit var policyAdapter: PolicyAdapter
    private lateinit var claimAdapter: ClaimAdapter
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var profileLayout: View
    private lateinit var titleText: TextView

    private var currentUserId: Long = -1L
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentUserId = intent.getLongExtra("USER_ID", 1L)

        recyclerView = findViewById(R.id.recyclerView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        profileLayout = findViewById(R.id.profileLayout)
        titleText = findViewById(R.id.titleText)
        
        findViewById<androidx.appcompat.widget.Toolbar?>(R.id.mainToolbar)?.let {
            setSupportActionBar(it)
            supportActionBar?.title = getString(R.string.app_name)
        }

        setupAdapters()
        setupNavigation()
        setupRefresh()
        observeViewModels()

        profileLayout.findViewById<Button>(R.id.upgradeSubscriptionButton).setOnClickListener {
            val companyId = profileViewModel.company.value?.id
            val avgScore = profileViewModel.subscriptions.value.firstOrNull()?.let { 
                agentViewModel.agents.value.map { it.guardrailScore ?: 0.0 }.average()
            } ?: 0.0

            if (companyId != null) {
                if (avgScore < 50.0) {
                    Toast.makeText(this, getString(R.string.upgrade_denied_message, avgScore), Toast.LENGTH_LONG).show()
                } else {
                    profileViewModel.purchaseTier(companyId, com.example.recourse.data.model.SubscriptionTier.INSURED)
                }
            }
        }

        profileLayout.findViewById<Button>(R.id.generateTrustSealButton).setOnClickListener {
            val companyName = profileViewModel.company.value?.name ?: getString(R.string.client_label)
            Toast.makeText(this, getString(R.string.trust_seal_generated_format, companyName), Toast.LENGTH_LONG).show()
        }

        profileLayout.findViewById<Button>(R.id.viewVentureRisksButton).setOnClickListener {
            startActivity(Intent(this, RiskRegisterActivity::class.java))
        }

        profileLayout.findViewById<Button>(R.id.viewRoadmapButton).setOnClickListener {
            startActivity(Intent(this, RoadmapActivity::class.java))
        }

        profileLayout.findViewById<Button>(R.id.viewGlossaryButton).setOnClickListener {
            startActivity(Intent(this, GlossaryActivity::class.java))
        }

        profileLayout.findViewById<Button>(R.id.viewMarketButton).setOnClickListener {
            startActivity(Intent(this, MarketIntelligenceActivity::class.java))
        }

        showAgents()
    }

    private fun setupAdapters() {
        agentAdapter = AiAgentAdapter { agent ->
            val intent = Intent(this, AgentDetailActivity::class.java).apply {
                putExtra("AGENT_ID", agent.id)
                putExtra("AGENT_NAME", agent.name)
                putExtra("AGENT_TYPE", agent.agentType.name)
                putExtra("AUTONOMY", agent.autonomyLevel.name)
                putExtra("STATUS", agent.status.name)
                putExtra("GUARDRAIL_SCORE", agent.guardrailScore ?: -1.0)
                putExtra("PLATFORM", agent.connectedPlatform ?: getString(R.string.direct_integration_label))
                putExtra("CONNECTED_AT", agent.connectedAt?.toString() ?: getString(R.string.na_label))
            }
            startActivity(intent)
        }
        incidentAdapter = IncidentAdapter { incident ->
            val intent = Intent(this, IncidentDetailActivity::class.java).apply {
                putExtra("INCIDENT_ID", incident.id)
                putExtra("SEVERITY", incident.severity.name)
                putExtra("STATUS", incident.status.name)
                putExtra("DESCRIPTION", incident.description)
                putExtra("FINANCIAL", incident.financialImpact?.let { getString(R.string.incident_financial_format, it, incident.currency ?: getString(R.string.default_currency)) } ?: "")
                putExtra("TRIGGER_EVENT_ID", incident.triggeringEventId ?: -1L)
                putExtra("REGION", profileViewModel.company.value?.regulatoryRegion?.name ?: getString(R.string.region_other))
            }
            startActivity(intent)
        }
        policyAdapter = PolicyAdapter { policy ->
            val intent = Intent(this, PolicyDetailActivity::class.java).apply {
                putExtra("POLICY_ID", policy.id)
                putExtra("COMPANY_ID", policy.companyId)
                putExtra("POLICY_NUMBER", policy.policyNumber)
                putExtra("COVERAGE_LIMIT", getString(R.string.coverage_limit_format, policy.coverageLimit, policy.currency))
                putExtra("REGION", profileViewModel.company.value?.regulatoryRegion?.name ?: getString(R.string.region_other))
            }
            startActivity(intent)
        }
        claimAdapter = ClaimAdapter { claim ->
            val intent = Intent(this, ClaimDetailActivity::class.java).apply {
                putExtra("CLAIM_NUMBER", claim.claimNumber)
                putExtra("STATUS", claim.status.name)
                putExtra("AMOUNT_CLAIMED", getString(R.string.coverage_limit_format, claim.amountClaimed, claim.currency))
                putExtra("AMOUNT_PAID", claim.amountPaid?.let { getString(R.string.coverage_limit_format, it, claim.currency) } ?: "")
                putExtra("INCIDENT_ID", claim.incidentId)
                putExtra("REGION", profileViewModel.company.value?.regulatoryRegion?.name ?: getString(R.string.region_other))
            }
            startActivity(intent)
        }
    }

    private fun setupNavigation() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_agents -> { showAgents(); true }
                R.id.nav_incidents -> { showIncidents(); true }
                R.id.nav_policies -> { showPolicies(); true }
                R.id.nav_claims -> { showClaims(); true }
                R.id.nav_profile -> { showProfile(); true }
                else -> false
            }
        }
    }

    private fun setupRefresh() {
        swipeRefresh.setOnRefreshListener {
            refreshCurrentTab()
        }
    }

    private fun refreshCurrentTab() {
        when (recyclerView.adapter) {
            agentAdapter -> agentViewModel.loadAgents()
            incidentAdapter -> incidentViewModel.loadIncidents()
            policyAdapter -> policyViewModel.loadPolicies()
            claimAdapter -> claimViewModel.loadClaims()
        }
        if (profileLayout.isVisible) {
            profileViewModel.loadProfile(currentUserId)
        }
    }

    private fun showAgents() {
        titleText.text = getString(R.string.agents_tab)
        recyclerView.isVisible = true
        swipeRefresh.isVisible = true
        profileLayout.isVisible = false
        recyclerView.adapter = agentAdapter
        agentViewModel.loadAgents()
    }

    private fun showIncidents() {
        titleText.text = getString(R.string.incidents_tab)
        recyclerView.isVisible = true
        swipeRefresh.isVisible = true
        profileLayout.isVisible = false
        recyclerView.adapter = incidentAdapter
        incidentViewModel.loadIncidents()
    }

    private fun showPolicies() {
        titleText.text = getString(R.string.policies_tab)
        recyclerView.isVisible = true
        swipeRefresh.isVisible = true
        profileLayout.isVisible = false
        recyclerView.adapter = policyAdapter
        policyViewModel.loadPolicies()
    }

    private fun showClaims() {
        titleText.text = getString(R.string.claims_tab)
        recyclerView.isVisible = true
        swipeRefresh.isVisible = true
        profileLayout.isVisible = false
        recyclerView.adapter = claimAdapter
        claimViewModel.loadClaims()
    }

    private fun showProfile() {
        titleText.text = getString(R.string.profile_tab)
        recyclerView.isVisible = false
        swipeRefresh.isVisible = true
        profileLayout.isVisible = true
        profileViewModel.loadProfile(currentUserId)
    }

    private fun observeViewModels() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    agentViewModel.agents.collect { agents ->
                        if (recyclerView.adapter == agentAdapter) {
                            agentAdapter.submitList(agents)
                            swipeRefresh.isRefreshing = false
                        }
                    }
                }
                launch {
                    incidentViewModel.incidents.collect { incidents ->
                        if (recyclerView.adapter == incidentAdapter) {
                            incidentAdapter.submitList(incidents)
                            swipeRefresh.isRefreshing = false
                        }
                    }
                }
                launch {
                    incidentViewModel.systemicAlert.collect { alert ->
                        val alertCard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.systemicAlertCard)
                        val alertView = findViewById<TextView>(R.id.systemicAlertText)
                        if (alert != null) {
                            alertCard.isVisible = true
                            alertView.text = alert
                        } else {
                            alertCard.isVisible = false
                        }
                    }
                }
                launch {
                    policyViewModel.policies.collect { policies ->
                        if (recyclerView.adapter == policyAdapter) {
                            policyAdapter.submitList(policies)
                            swipeRefresh.isRefreshing = false
                        }
                    }
                }
                launch {
                    claimViewModel.claims.collect { claims ->
                        if (recyclerView.adapter == claimAdapter) {
                            claimAdapter.submitList(claims)
                            swipeRefresh.isRefreshing = false
                        }
                    }
                }
                launch {
                    profileViewModel.user.collect { user ->
                        user?.let {
                            profileLayout.findViewById<TextView>(com.example.recourse.R.id.userName).text = it.fullName
                            profileLayout.findViewById<TextView>(com.example.recourse.R.id.userEmail).text = it.email
                            profileLayout.findViewById<TextView>(com.example.recourse.R.id.userRole).text = getString(R.string.user_role_format, it.role.name)
                            
                            findViewById<TextView>(R.id.personaWelcomeText).text = getString(R.string.authenticated_format, it.fullName, it.role.name)
                            
                            val isBroker = it.role.name == "BROKER"
                            profileLayout.findViewById<View>(com.example.recourse.R.id.brokerCapacityBadge).isVisible = isBroker
                            profileLayout.findViewById<View>(com.example.recourse.R.id.brokerCommissionText).isVisible = isBroker
                            profileLayout.findViewById<View>(com.example.recourse.R.id.viewMarketButton).isVisible = isBroker

                            val isExec = it.role.name == "COO_CFO" || it.role.name == "ADMIN"
                            profileLayout.findViewById<View>(com.example.recourse.R.id.viewVentureRisksButton).isVisible = isExec
                            profileLayout.findViewById<View>(com.example.recourse.R.id.viewRoadmapButton).isVisible = isExec
                            
                            findViewById<TextView>(R.id.personaValueProp).text = when(it.role.name) {
                                "COO_CFO" -> getString(R.string.value_prop_cfo)
                                "HEAD_OF_AI" -> getString(R.string.value_prop_ai)
                                "GENERAL_COUNSEL" -> getString(R.string.value_prop_gc)
                                "BROKER" -> getString(R.string.value_prop_broker)
                                else -> getString(R.string.value_prop_default)
                            }
                        }
                    }
                }
                launch {
                    profileViewModel.company.collect { company ->
                        company?.let {
                            profileLayout.findViewById<TextView>(com.example.recourse.R.id.companyName).text = it.name
                            profileLayout.findViewById<TextView>(com.example.recourse.R.id.companyIndustry).text = getString(R.string.company_industry_label, it.industry)
                            profileLayout.findViewById<TextView>(com.example.recourse.R.id.companyRegion).text = getString(R.string.company_region_format, it.regulatoryRegion.name)
                            
                            profileLayout.findViewById<TextView>(com.example.recourse.R.id.industryBenchmarkText).text = when(it.industry) {
                                "E-commerce / D2C Retail" -> getString(R.string.industry_benchmark_format, "68%")
                                "Fintech / Lending" -> getString(R.string.industry_benchmark_format, "74%")
                                else -> getString(R.string.industry_benchmark_format, "60%")
                            }
                            
                            val sandboxView = profileLayout.findViewById<TextView>(com.example.recourse.R.id.sandboxStatus)
                            val sandboxText = when(it.regulatoryRegion.name) {
                                "SINGAPORE_ASEAN" -> getString(R.string.sandbox_mas)
                                "UAE_GCC" -> getString(R.string.sandbox_adgm)
                                "EU_UK" -> getString(R.string.sandbox_eu)
                                else -> null
                            }
                            if (sandboxText != null) {
                                sandboxView.isVisible = true
                                sandboxView.text = sandboxText
                            } else {
                                sandboxView.isVisible = false
                            }
                        }
                    }
                }
                launch {
                    profileViewModel.subscriptions.collect { subs ->
                        val summary = subs.joinToString("\n") { sub ->
                            getString(R.string.subscription_item_format, sub.tier.name, sub.status.name, sub.startDate)
                        }
                        profileLayout.findViewById<TextView>(com.example.recourse.R.id.subscriptionSummary).text = summary.ifEmpty { getString(R.string.no_active_subscriptions) }
                        swipeRefresh.isRefreshing = false
                    }
                }
                launch {
                    profileViewModel.totalClaimed.collect { total ->
                        profileLayout.findViewById<TextView>(com.example.recourse.R.id.totalClaimedText).text = currencyFormatter.format(total)
                    }
                }
                launch {
                    profileViewModel.lossRatio.collect { ratio ->
                        profileLayout.findViewById<TextView>(com.example.recourse.R.id.lossRatioText).text = getString(R.string.loss_ratio_format, ratio * 100)
                    }
                }
                launch {
                    profileViewModel.riskReduction.collect { reduction ->
                        profileLayout.findViewById<TextView>(com.example.recourse.R.id.riskReductionText).text = getString(R.string.risk_reduction_format, reduction)
                    }
                }
                launch {
                    profileViewModel.avgResolutionTime.collect { avg ->
                        profileLayout.findViewById<TextView>(com.example.recourse.R.id.avgRecoveryTimeText).text = getString(R.string.avg_resolution_format, avg)
                    }
                }
                launch {
                    profileViewModel.timeSavedDays.collect { saved ->
                        profileLayout.findViewById<TextView>(com.example.recourse.R.id.timeSavedText).text = getString(R.string.efficiency_gain_format, saved)
                    }
                }
                launch {
                    profileViewModel.connectedAgentsCount.collect { count ->
                        profileLayout.findViewById<TextView>(com.example.recourse.R.id.guardrailTrendText).text = getString(R.string.active_monitors_format, count)
                    }
                }
                launch {
                    profileViewModel.committedCapacity.collect { capacity ->
                        profileLayout.findViewById<TextView>(com.example.recourse.R.id.committedCapacityText).text = getString(R.string.committed_capacity_format, currencyFormatter.format(capacity))
                    }
                }
                launch {
                    profileViewModel.brokerReferralRate.collect { rate ->
                        profileLayout.findViewById<TextView>(com.example.recourse.R.id.brokerReferralText).text = getString(R.string.broker_channel_format, rate)
                    }
                }
                launch {
                    profileViewModel.purchaseSuccess.collect { success ->
                        if (success == true) {
                            Toast.makeText(this@MainActivity, getString(R.string.tier_upgrade_success), Toast.LENGTH_LONG).show()
                            profileViewModel.loadProfile(currentUserId)
                        } else if (success == false) {
                            Toast.makeText(this@MainActivity, getString(R.string.upgrade_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
