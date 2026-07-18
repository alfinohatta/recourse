package com.example.recourse.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.recourse.R

class RiskRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_risk_register)

        val container: LinearLayout = findViewById(R.id.riskContainer)
        
        val risks = listOf(
            Pair(getString(R.string.risk_accelerator), getString(R.string.mit_accelerator)),
            Pair(getString(R.string.risk_adverse_selection), getString(R.string.mit_adverse_selection)),
            Pair(getString(R.string.risk_thin_data), getString(R.string.mit_thin_data)),
            Pair(getString(R.string.risk_correlated_loss), getString(R.string.mit_correlated_loss)),
            Pair(getString(R.string.risk_regulatory_block), getString(R.string.mit_regulatory_block))
        )

        risks.forEach { (name, mitigation) ->
            val itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, container, false)
            itemView.findViewById<TextView>(android.R.id.text1).apply {
                text = getString(R.string.risk_item_format, name)
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(0xFFB71C1C.toInt())
            }
            itemView.findViewById<TextView>(android.R.id.text2).text = mitigation
            container.addView(itemView)
        }

        findViewById<Button>(R.id.backToProfileButton).setOnClickListener {
            finish()
        }
    }
}
