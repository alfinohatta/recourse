package com.example.recourse.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.recourse.R
import com.example.recourse.data.model.IncidentStatus
import com.example.recourse.data.model.Severity
import com.example.recourse.ui.viewmodel.IncidentDetailViewModel
import kotlinx.coroutines.launch

class IncidentDetailActivity : AppCompatActivity() {

    private val viewModel: IncidentDetailViewModel by viewModels()
    private var incidentId: Long = -1L
    private var currentStatus: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incident_detail)

        incidentId = intent.getLongExtra("INCIDENT_ID", -1L)
        val severity = intent.getStringExtra("SEVERITY") ?: ""
        currentStatus = intent.getStringExtra("STATUS") ?: ""
        val description = intent.getStringExtra("DESCRIPTION") ?: ""
        val financial = intent.getStringExtra("FINANCIAL") ?: ""
        val triggerEventId = intent.getLongExtra("TRIGGER_EVENT_ID", -1L)
        val region = intent.getStringExtra("REGION") ?: getString(R.string.region_other)

        setupToolbar(getString(R.string.incident_details_label))
        setupIncidentView(severity, currentStatus, description, financial, region)
        observeViewModel()

        findViewById<Button>(R.id.confirmButton).setOnClickListener {
            if (incidentId != -1L) viewModel.updateStatus(incidentId, IncidentStatus.CONFIRMED)
        }

        findViewById<Button>(R.id.falsePositiveButton).setOnClickListener {
            if (incidentId != -1L) viewModel.updateStatus(incidentId, IncidentStatus.FALSE_POSITIVE)
        }

        findViewById<Button>(R.id.resolveButton).setOnClickListener {
            if (incidentId != -1L) viewModel.updateStatus(incidentId, IncidentStatus.RESOLVED)
        }

        findViewById<Button>(R.id.openClaimButton).setOnClickListener {
            val amountStr = financial.split(" ")[0]
            val currencyStr = if (financial.split(" ").size > 1) financial.split(" ")[1] else "USD"
            val amount = amountStr.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
            if (incidentId != -1L) {
                viewModel.openClaim(incidentId, amount, currencyStr)
            }
        }

        findViewById<Button>(R.id.escalateButton).setOnClickListener {
            Toast.makeText(this, getString(R.string.escalated_msg), Toast.LENGTH_LONG).show()
            finish()
        }

        if (triggerEventId != -1L) {
            viewModel.loadTriggerEvent(triggerEventId)
        }
        if (incidentId != -1L) {
            viewModel.checkExistingClaim(incidentId)
        }
    }

    private fun setupToolbar(title: String) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupIncidentView(severity: String, status: String, desc: String, financial: String, region: String) {
        val severityText: TextView = findViewById(R.id.detailSeverity)
        severityText.text = severity
        
        val color = when(severity) {
            Severity.CRITICAL.name -> 0xFFD32F2F.toInt()
            Severity.HIGH.name -> 0xFFF57C00.toInt()
            Severity.MEDIUM.name -> 0xFFFBC02D.toInt()
            else -> 0xFF1976D2.toInt()
        }
        severityText.background.setTint(color)
        
        findViewById<TextView>(R.id.detailStatus).text = getString(R.string.incident_status_format, status)
        findViewById<TextView>(R.id.detailDescription).text = desc
        
        val financialView = findViewById<TextView>(R.id.detailFinancial)
        financialView.text = financial
        
        if (desc.contains("messaging", ignoreCase = true) || desc.contains("jurisdiction", ignoreCase = true)) {
            val fineRisk = when(region) {
                "EU_UK" -> getString(R.string.risk_gdpr)
                "UAE_GCC" -> getString(R.string.risk_difc)
                "SINGAPORE_ASEAN" -> getString(R.string.risk_pdpa)
                else -> getString(R.string.risk_general)
            }
            financialView.text = getString(R.string.compliance_alert_format, financial, fineRisk)
        }

        if (status == IncidentStatus.FLAGGED.name) {
             findViewById<TextView>(R.id.detailStatus).text = getString(R.string.status_verifying_triggers)
        }
        
        val jurisdictionText = when(region) {
            "EU_UK" -> getString(R.string.jurisdiction_eu)
            "US" -> getString(R.string.jurisdiction_us)
            "SINGAPORE_ASEAN" -> getString(R.string.jurisdiction_sg)
            "UAE_GCC" -> getString(R.string.jurisdiction_uae)
            else -> getString(R.string.jurisdiction_default)
        }
        findViewById<TextView>(R.id.detailJurisdiction).text = jurisdictionText

        updateActionButtons(status)
    }

    private fun updateActionButtons(status: String) {
        val confirmBtn = findViewById<Button>(R.id.confirmButton)
        val falsePositiveBtn = findViewById<Button>(R.id.falsePositiveButton)
        val resolveBtn = findViewById<Button>(R.id.resolveButton)
        val openClaimBtn = findViewById<Button>(R.id.openClaimButton)
        val escalateBtn = findViewById<Button>(R.id.escalateButton)
        
        when (status) {
            IncidentStatus.FLAGGED.name, IncidentStatus.INVESTIGATING.name -> {
                confirmBtn.isVisible = true
                falsePositiveBtn.isVisible = true
                resolveBtn.isVisible = true
                openClaimBtn.isVisible = false
                escalateBtn.isVisible = false
            }
            IncidentStatus.CONFIRMED.name -> {
                confirmBtn.isVisible = false
                falsePositiveBtn.isVisible = true
                resolveBtn.isVisible = true
                
                if (viewModel.matchedThreshold.value != null) {
                    openClaimBtn.isVisible = true
                    escalateBtn.isVisible = false
                } else {
                    openClaimBtn.isVisible = false
                    escalateBtn.isVisible = true
                }
            }
            else -> {
                confirmBtn.isVisible = false
                falsePositiveBtn.isVisible = false
                resolveBtn.isVisible = false
                openClaimBtn.isVisible = false
                escalateBtn.isVisible = false
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.triggerEvent.collect { event ->
                        val card = findViewById<View>(R.id.eventCard)
                        val noEvent = findViewById<View>(R.id.noEventText)
                        if (event != null) {
                            card.isVisible = true
                            noEvent.isVisible = false
                            findViewById<TextView>(R.id.triggerEventType).text = event.eventType.name
                            findViewById<TextView>(R.id.triggerEventMetadata).text = event.metadata
                        } else {
                            card.isVisible = false
                            noEvent.isVisible = true
                        }
                    }
                }
                launch {
                    viewModel.matchedThreshold.collect { threshold ->
                        val thresholdText = findViewById<TextView>(R.id.triggeredThresholdText)
                        val divider = findViewById<View>(R.id.thresholdDivider)
                        val autoBadge = findViewById<TextView>(R.id.autoClaimBadge)
                        if (threshold != null) {
                            divider.isVisible = true
                            thresholdText.isVisible = true
                            thresholdText.text = getString(R.string.crossed_threshold_format, threshold.description)
                            autoBadge.isVisible = true
                        } else {
                            divider.isVisible = false
                            thresholdText.isVisible = false
                            autoBadge.isVisible = false
                        }
                    }
                }
                launch {
                    viewModel.updateSuccess.collect { success ->
                        if (success == true) {
                            Toast.makeText(this@IncidentDetailActivity, getString(R.string.status_updated_msg), Toast.LENGTH_SHORT).show()
                            finish() 
                        } else if (success == false) {
                            Toast.makeText(this@IncidentDetailActivity, getString(R.string.update_failed_msg), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                launch {
                    viewModel.existingClaim.collect { claim ->
                        val openClaimBtn = findViewById<Button>(R.id.openClaimButton)
                        if (claim != null) {
                            openClaimBtn.isEnabled = false
                            openClaimBtn.text = getString(R.string.claim_already_submitted_format, claim.claimNumber)
                        }
                    }
                }
                launch {
                    viewModel.claimSuccess.collect { success ->
                        if (success == true) {
                            Toast.makeText(this@IncidentDetailActivity, getString(R.string.payout_initiated_msg), Toast.LENGTH_LONG).show()
                            viewModel.checkExistingClaim(incidentId)
                        } else if (success == false) {
                            Toast.makeText(this@IncidentDetailActivity, getString(R.string.claim_submission_failed_msg), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
