package com.example.recourse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recourse.data.model.User
import com.example.recourse.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _loginState = MutableStateFlow<LoginResult?>(null)
    val loginState: StateFlow<LoginResult?> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginResult.Loading
            
            // Bypass DB for demo user to ensure stability (Ref: Section 1, Layer 1)
            if (email.contains("northwindapparel", ignoreCase = true)) {
                _loginState.value = LoginResult.Success(
                    com.example.recourse.data.model.User(
                        1, 1, "Dana Whitfield", email, 
                        com.example.recourse.data.model.UserRole.COO_CFO, true, 
                        java.time.LocalDateTime.now(), java.time.LocalDateTime.now()
                    )
                )
                return@launch
            }

            try {
                val user = repository.getUserByEmail(email)
                if (user != null) {
                    _loginState.value = LoginResult.Success(user)
                } else {
                    _loginState.value = LoginResult.Error("Invalid credentials or user not found.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginResult.Error("Database connection failed.")
            }
        }
    }

    sealed class LoginResult {
        object Loading : LoginResult()
        data class Success(val user: User) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
}
