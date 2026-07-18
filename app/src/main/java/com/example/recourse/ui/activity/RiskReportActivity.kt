package com.example.recourse.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.recourse.R

class RiskReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_risk_report)

        val agentName = intent.getStringExtra("AGENT_NAME") ?: getString(R.string.unknown_agent_label)
        val autonomy = intent.getStringExtra("AUTONOMY") ?: "MEDIUM"
        val score = intent.getDoubleExtra("GUARDRAIL_SCORE", 0.0)
        val region = intent.getStringExtra("REGION") ?: getString(R.string.region_other)

        findViewById<TextView>(R.id.reportAgentName).text = agentName

        val riskLevel = when {
            autonomy == "FULL" && score < 50 -> getString(R.string.risk_level_critical)
            autonomy == "HIGH" && score < 60 -> getString(R.string.risk_level_high)
            score < 40 -> getString(R.string.risk_level_high)
            score > 80 -> getString(R.string.risk_level_low)
            else -> getString(R.string.risk_level_moderate)
        }

        val regulatoryContext = when(region) {
            "EU_UK" -> getString(R.string.reg_eu)
            "US" -> getString(R.string.reg_us)
            "SINGAPORE_ASEAN" -> getString(R.string.reg_sg)
            "UAE_GCC" -> getString(R.string.reg_uae)
            else -> getString(R.string.reg_default)
        }
        findViewById<TextView>(R.id.reportComplianceText).text = regulatoryContext

        // Show PIA for UAE/DIFC (Ref: PROJECT.MD Section 4, Layer 1)
        findViewById<View>(R.id.piaCard).isVisible = region == "UAE_GCC"

        val summary = getString(R.string.risk_report_summary_format, agentName, riskLevel, autonomy, score.toInt())
        findViewById<TextView>(R.id.reportSummaryText).text = summary

        // Detailed Guardrail Audit (Ref: PROJECT.MD Section 4, Layer 1)
        val safeguards = StringBuilder()
        safeguards.append(getString(R.string.safeguard_spending_format, if (score > 60) getString(R.string.safeguard_active) else getString(R.string.safeguard_partial))).append("\n")
        safeguards.append(getString(R.string.safeguard_approval_format, if (autonomy == "LOW" || autonomy == "MEDIUM") getString(R.string.safeguard_active_hitl) else getString(R.string.safeguard_bypassed))).append("\n")
        safeguards.append(getString(R.string.safeguard_undo_format, if (score > 75) getString(R.string.safeguard_supported) else getString(R.string.safeguard_not_detected))).append("\n")
        safeguards.append(getString(R.string.safeguard_telemetry_active))
        
        findViewById<TextView>(R.id.reportSafeguardsText).text = safeguards.toString()

        findViewById<Button>(R.id.exportLogButton).setOnClickListener {
            Toast.makeText(this, getString(R.string.log_generated_msg), Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.closeReportButton).setOnClickListener {
            finish()
        }
    }
}
