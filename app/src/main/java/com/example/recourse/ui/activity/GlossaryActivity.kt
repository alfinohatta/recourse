package com.example.recourse.ui.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.recourse.R

class GlossaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glossary)

        val container: LinearLayout = findViewById(R.id.glossaryContainer)
        
        val terms = listOf(
            Pair(getString(R.string.term_silent_ai), getString(R.string.def_silent_ai)),
            Pair(getString(R.string.term_parametric), getString(R.string.def_parametric)),
            Pair(getString(R.string.term_mga), getString(R.string.def_mga)),
            Pair(getString(R.string.term_loss_ratio), getString(R.string.def_loss_ratio)),
            Pair(getString(R.string.term_sandbox), getString(R.string.def_sandbox))
        )

        terms.forEach { (term, definition) ->
            val itemView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, container, false)
            itemView.findViewById<TextView>(android.R.id.text1).apply {
                text = term
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(0xFF2E7D32.toInt())
            }
            itemView.findViewById<TextView>(android.R.id.text2).text = definition
            container.addView(itemView)
        }

        findViewById<Button>(R.id.closeGlossaryButton).setOnClickListener {
            finish()
        }
    }
}
