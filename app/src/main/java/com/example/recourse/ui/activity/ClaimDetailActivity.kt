package com.example.recourse.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.recourse.R
import com.example.recourse.data.model.Severity
import com.example.recourse.ui.viewmodel.ClaimDetailViewModel
import kotlinx.coroutines.launch

class ClaimDetailActivity : AppCompatActivity() {

    private val viewModel: ClaimDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_claim_detail)

        val claimNumber = intent.getStringExtra("CLAIM_NUMBER") ?: getString(R.string.claims_tab)
        val status = intent.getStringExtra("STATUS") ?: ""
        val amountClaimed = intent.getStringExtra("AMOUNT_CLAIMED") ?: ""
        val amountPaid = intent.getStringExtra("AMOUNT_PAID") ?: ""
        val incidentId = intent.getLongExtra("INCIDENT_ID", -1L)
        val region = intent.getStringExtra("REGION") ?: getString(R.string.region_other)

        setupToolbar(claimNumber)
        setupClaimView(claimNumber, status, amountClaimed, amountPaid, region)
        observeViewModel()

        if (incidentId != -1L) {
            viewModel.loadIncident(incidentId)
        }
    }

    private fun setupToolbar(number: String) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = number
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClaimView(number: String, status: String, claimed: String, paid: String, region: String) {
        findViewById<TextView>(R.id.detailClaimNumber).text = number
        findViewById<TextView>(R.id.detailClaimStatus).text = getString(R.string.claim_status_format, status)
        findViewById<TextView>(R.id.detailClaimAmounts).text = getString(R.string.claim_amounts_format, claimed, paid.ifEmpty { getString(R.string.pending_label) })
        
        val jurisdictionText = when(region) {
            "EU_UK" -> getString(R.string.jurisdiction_eu)
            "US" -> getString(R.string.jurisdiction_us)
            "SINGAPORE_ASEAN" -> getString(R.string.jurisdiction_sg)
            "UAE_GCC" -> getString(R.string.jurisdiction_uae)
            else -> getString(R.string.jurisdiction_default)
        }
        findViewById<TextView>(R.id.detailJurisdiction).text = jurisdictionText
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.linkedIncident.collect { incident ->
                    val card = findViewById<View>(R.id.incidentCard)
                    if (incident != null) {
                        card.isVisible = true
                        val severityText: TextView = findViewById(R.id.linkedIncidentSeverity)
                        severityText.text = incident.severity.name
                        
                        val color = when(incident.severity) {
                            Severity.CRITICAL -> 0xFFD32F2F.toInt()
                            Severity.HIGH -> 0xFFF57C00.toInt()
                            Severity.MEDIUM -> 0xFFFBC02D.toInt()
                            else -> 0xFF1976D2.toInt()
                        }
                        severityText.background.setTint(color)
                        
                        findViewById<TextView>(R.id.linkedIncidentDescription).text = incident.description
                    } else {
                        card.isVisible = false
                    }
                }
            }
        }
    }
}
