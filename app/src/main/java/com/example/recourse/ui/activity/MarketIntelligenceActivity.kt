package com.example.recourse.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.recourse.R

class MarketIntelligenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market_intelligence)

        val container: LinearLayout = findViewById(R.id.marketContainer)
        
        val competitors = listOf(
            Triple("Armilla AI", getString(R.string.comp_armilla_strength), getString(R.string.comp_armilla_wedge)),
            Triple("Coalition", getString(R.string.comp_coalition_strength), getString(R.string.comp_coalition_wedge)),
            Triple("Munich Re (aiSure)", getString(R.string.comp_munich_strength), getString(R.string.comp_munich_wedge)),
            Triple("Testudo", getString(R.string.comp_testudo_strength), getString(R.string.comp_testudo_wedge))
        )

        competitors.forEach { (name, strength, wedge) ->
            val itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, container, false)
            itemView.findViewById<TextView>(android.R.id.text1).apply {
                text = getString(R.string.competitor_format, name)
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(0xFF0D47A1.toInt())
            }
            itemView.findViewById<TextView>(android.R.id.text2).text = getString(R.string.comp_info_format, strength, wedge)
            container.addView(itemView)
        }

        findViewById<Button>(R.id.closeMarketButton).setOnClickListener {
            finish()
        }
    }
}
