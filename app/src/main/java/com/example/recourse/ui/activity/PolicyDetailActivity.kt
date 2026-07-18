package com.example.recourse.ui.activity

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.recourse.R
import com.example.recourse.ui.adapter.PolicyThresholdAdapter
import com.example.recourse.ui.viewmodel.PolicyThresholdViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class PolicyDetailActivity : AppCompatActivity() {

    private val viewModel: PolicyThresholdViewModel by viewModels()
    private lateinit var adapter: PolicyThresholdAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_policy_detail)

        val policyId = intent.getLongExtra("POLICY_ID", -1L)
        val companyId = intent.getLongExtra("COMPANY_ID", -1L)
        val region = intent.getStringExtra("REGION") ?: getString(R.string.region_other)
        val policyNumber = intent.getStringExtra("POLICY_NUMBER") ?: getString(R.string.policies_tab)
        val coverageLimit = intent.getStringExtra("COVERAGE_LIMIT") ?: ""

        swipeRefresh = findViewById(R.id.policyDetailSwipeRefresh)

        setupToolbar(policyNumber)
        setupDetails(policyNumber, coverageLimit, region)
        setupRecyclerView()
        observeViewModel()

        swipeRefresh.setOnRefreshListener {
            if (policyId != -1L) {
                viewModel.loadThresholds(policyId)
                if (companyId != -1L) viewModel.calculateRenewalEstimate(policyId, companyId)
            }
        }

        if (policyId != -1L) {
            viewModel.loadThresholds(policyId)
            if (companyId != -1L) viewModel.calculateRenewalEstimate(policyId, companyId)
        }
    }

    private fun setupToolbar(number: String) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = number
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDetails(number: String, limit: String, region: String) {
        findViewById<TextView>(R.id.detailPolicyNumber).text = number
        findViewById<TextView>(R.id.detailPolicyInfo).text = limit
        
        val weighting = when(region) {
            "EU_UK" -> getString(R.string.weight_eu)
            "UAE_GCC" -> getString(R.string.weight_uae)
            "SINGAPORE_ASEAN" -> getString(R.string.weight_sg)
            else -> getString(R.string.weight_default)
        }
        findViewById<TextView>(R.id.regionalWeightingText).text = weighting

        val roadmap = when(region) {
            "EU_UK" -> getString(R.string.roadmap_eu)
            "UAE_GCC" -> getString(R.string.roadmap_uae)
            "SINGAPORE_ASEAN" -> getString(R.string.roadmap_sg)
            else -> getString(R.string.roadmap_default)
        }
        findViewById<TextView>(R.id.regionalRoadmapText).text = roadmap
    }

    private fun setupRecyclerView() {
        adapter = PolicyThresholdAdapter()
        findViewById<RecyclerView>(R.id.thresholdRecyclerView).adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.thresholds.collect { thresholds ->
                        adapter.submitList(thresholds)
                        swipeRefresh.isRefreshing = false
                    }
                }
                launch {
                    viewModel.renewalEstimate.collect { estimate ->
                        if (estimate != null) {
                            findViewById<TextView>(R.id.renewalEstimateText).text = 
                                getString(R.string.next_year_premium_format, currencyFormatter.format(estimate))
                        }
                    }
                }
            }
        }
    }
}
