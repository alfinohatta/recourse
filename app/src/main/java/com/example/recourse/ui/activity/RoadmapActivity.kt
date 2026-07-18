package com.example.recourse.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.recourse.R

class RoadmapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roadmap)

        val container: LinearLayout = findViewById(R.id.roadmapContainer)
        
        val phases = listOf(
            Pair("Year 1: Foundation", "Build Assess/Monitor products. Launch in D2C beachhead via MAS/ADGM sandboxes. Collect loss data."),
            Pair("Year 2: Insurance Launch", "Bind first Insured-tier policies. Secure A-Rated capacity. Formalize broker distribution."),
            Pair("Year 3: Vertical Expansion", "Extend into Fintech persona. Publish aggregated Claims Report. Begin EU/UK full licensing."),
            Pair("Year 4+: Platform Distribution", "Embedded white-label distribution via agent-orchestration and CS platforms.")
        )

        phases.forEach { (phase, details) ->
            val itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, container, false)
            itemView.findViewById<TextView>(android.R.id.text1).apply {
                text = phase
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(0xFF0288D1.toInt()) // Light Blue 700
            }
            itemView.findViewById<TextView>(android.R.id.text2).text = details
            container.addView(itemView)
        }

        findViewById<Button>(R.id.closeRoadmapButton).setOnClickListener {
            finish()
        }
    }
}
