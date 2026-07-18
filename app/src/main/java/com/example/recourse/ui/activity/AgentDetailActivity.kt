package com.example.recourse.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import com.example.recourse.data.model.AgentStatus
import com.example.recourse.ui.adapter.AgentEventAdapter
import com.example.recourse.ui.viewmodel.AiAgentDetailViewModel
import kotlinx.coroutines.launch

class AgentDetailActivity : AppCompatActivity() {

    private val viewModel: AiAgentDetailViewModel by viewModels()
    private lateinit var adapter: AgentEventAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private var currentStatus: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agent_detail)

        val agentId = intent.getLongExtra("AGENT_ID", -1L)
        val agentName = intent.getStringExtra("AGENT_NAME") ?: getString(R.string.agents_tab)
        val agentType = intent.getStringExtra("AGENT_TYPE") ?: ""
        val platform = intent.getStringExtra("PLATFORM") ?: getString(R.string.direct_integration_label)
        val autonomy = intent.getStringExtra("AUTONOMY") ?: ""
        val guardrailScore = intent.getDoubleExtra("GUARDRAIL_SCORE", -1.0)
        currentStatus = intent.getStringExtra("STATUS") ?: ""

        val autoPauseSwitch = findViewById<androidx.appcompat.widget.SwitchCompat>(com.example.recourse.R.id.autoPauseSwitch)
        autoPauseSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoPause(isChecked)
            val msg = if (isChecked) getString(R.string.auto_pause_enabled_msg) 
                      else getString(R.string.auto_pause_disabled_msg)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        swipeRefresh = findViewById(R.id.agentDetailSwipeRefresh)

        setupToolbar(agentName)
        setupDetails(agentName, agentType, platform, autonomy, currentStatus, guardrailScore)
        setupRecyclerView()
        observeViewModel()

        findViewById<Button>(R.id.statusToggleButton).setOnClickListener {
            if (agentId != -1L) {
                viewModel.toggleStatus(agentId, currentStatus)
            }
        }

        findViewById<Button>(R.id.viewRiskReportButton).setOnClickListener {
            val intent = Intent(this, RiskReportActivity::class.java).apply {
                putExtra("AGENT_NAME", agentName)
                putExtra("AUTONOMY", autonomy)
                putExtra("GUARDRAIL_SCORE", guardrailScore)
                putExtra("REGION", viewModel.company.value?.regulatoryRegion?.name ?: getString(R.string.region_other))
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.simulateEdgeCaseButton).setOnClickListener {
            if (agentId != -1L) {
                viewModel.simulateAnomaly(agentId, com.example.recourse.data.model.AgentType.valueOf(agentType))
                Toast.makeText(this, getString(R.string.injecting_anomaly_msg), Toast.LENGTH_SHORT).show()
            }
        }

        swipeRefresh.setOnRefreshListener {
            if (agentId != -1L) viewModel.loadEvents(agentId)
        }

        if (agentId != -1L) {
            viewModel.loadEvents(agentId)
        }
    }

    private fun setupToolbar(name: String) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = name
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDetails(name: String, type: String, platform: String, autonomy: String, status: String, score: Double) {
        findViewById<TextView>(R.id.detailAgentName).text = name
        findViewById<TextView>(R.id.detailAgentInfo).text = getString(R.string.autonomy_status_format, type, autonomy, status)
        findViewById<TextView>(R.id.detailAgentPlatform).text = getString(R.string.connected_platform_format, platform)
        
        val progressBar = findViewById<ProgressBar>(R.id.guardrailProgressBar)
        val scorePercent = findViewById<TextView>(R.id.guardrailPercentText)
        
        if (score >= 0) {
            scorePercent.text = getString(R.string.percent_format, score.toInt())
            progressBar.progress = score.toInt()
        } else {
            scorePercent.text = getString(R.string.percent_na)
            progressBar.progress = 0
        }
        
        updateToggleButton(status)
    }

    private fun updateToggleButton(status: String) {
        val btn = findViewById<Button>(R.id.statusToggleButton)
        btn.text = if (status == AgentStatus.ACTIVE.name) getString(R.string.pause_agent_label) else getString(R.string.resume_agent_label)
    }

    private fun setupRecyclerView() {
        adapter = AgentEventAdapter()
        findViewById<RecyclerView>(R.id.eventRecyclerView).adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.events.collect { events ->
                        adapter.submitList(events)
                        swipeRefresh.isRefreshing = false
                    }
                }
                launch {
                    viewModel.insights.collect { insight ->
                        val insightText = findViewById<TextView>(com.example.recourse.R.id.agentInsightText)
                        if (insight != null) {
                            insightText.isVisible = true
                            insightText.text = insight
                        } else {
                            insightText.isVisible = false
                        }
                    }
                }
                launch {
                    viewModel.thresholdPulse.collect { pulse ->
                        val pulseText = findViewById<TextView>(R.id.thresholdPulseText)
                        if (pulse != null) {
                            pulseText.isVisible = true
                            pulseText.text = pulse
                        } else {
                            pulseText.isVisible = false
                        }
                    }
                }
                launch {
                    viewModel.updateSuccess.collect { success ->
                        if (success == true) {
                            currentStatus = if (currentStatus == AgentStatus.ACTIVE.name) {
                                AgentStatus.PAUSED.name
                            } else {
                                AgentStatus.ACTIVE.name
                            }
                            findViewById<TextView>(R.id.detailAgentInfo).text = 
                                getString(R.string.autonomy_status_format, intent.getStringExtra("AGENT_TYPE"), intent.getStringExtra("AUTONOMY"), currentStatus)
                            updateToggleButton(currentStatus)
                            Toast.makeText(this@AgentDetailActivity, getString(R.string.agent_status_updated), Toast.LENGTH_SHORT).show()
                        } else if (success == false) {
                            Toast.makeText(this@AgentDetailActivity, getString(R.string.failed_update_status), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
