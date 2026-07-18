package com.example.recourse.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.recourse.MainActivity
import com.example.recourse.R
import com.example.recourse.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val progress = findViewById<ProgressBar>(R.id.loginProgress)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            if (email.contains("northwindapparel", ignoreCase = true)) {
                // Instant Success for CFO Demo
                val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                    putExtra("USER_ID", 1L)
                }
                startActivity(intent)
                finish()
                return@setOnClickListener
            }

            if (email.contains("broker", ignoreCase = true)) {
                // Instant Success for Broker Demo
                val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                    putExtra("USER_ID", 6L) // Sam Alvarado is ID 6
                }
                startActivity(intent)
                finish()
                return@setOnClickListener
            }
            
            if (email.isNotEmpty()) {
                viewModel.login(email, "")
            } else {
                Toast.makeText(this, getString(R.string.enter_email_msg), Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { result ->
                    when (result) {
                        is LoginViewModel.LoginResult.Loading -> {
                            progress.isVisible = true
                            loginButton.isEnabled = false
                        }
                        is LoginViewModel.LoginResult.Success -> {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                                putExtra("USER_ID", result.user.id)
                            }
                            startActivity(intent)
                            finish()
                        }
                        is LoginViewModel.LoginResult.Error -> {
                            progress.isVisible = false
                            loginButton.isEnabled = true
                            Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()
                        }
                        null -> {}
                    }
                }
            }
        }
    }
}
