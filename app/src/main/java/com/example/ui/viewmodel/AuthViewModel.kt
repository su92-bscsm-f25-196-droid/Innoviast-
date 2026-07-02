package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserEntity
import com.example.data.repository.LmsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LmsRepository(application)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _verificationMessage = MutableStateFlow<String?>(null)
    val verificationMessage: StateFlow<String?> = _verificationMessage.asStateFlow()

    init {
        // Initialize database and auto-login if "Remember Me" is enabled
        viewModelScope.launch {
            repository.initDatabaseWithSeedData()
            checkAutoLogin()
        }
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val rememberedUser = repository.dao.getRememberedUser()
            if (rememberedUser != null && rememberedUser.isActive) {
                _authState.value = AuthState.Authenticated(rememberedUser)
            } else {
                _authState.value = AuthState.Idle
            }
        }
    }

    fun login(email: String, passwordHash: String, rememberMe: Boolean) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = repository.dao.getUserByEmail(email)
            if (user == null) {
                _authState.value = AuthState.Error("Account not found with this email.")
                return@launch
            }

            if (!user.isActive) {
                _authState.value = AuthState.Error("This account has been deactivated by the Admin.")
                return@launch
            }

            // In our professional demo, we check password hashes
            if (user.passwordHash == passwordHash) {
                if (rememberMe) {
                    repository.dao.clearRememberedUsers()
                    val updatedUser = user.copy(isRemembered = true)
                    repository.dao.insertUser(updatedUser)
                    _authState.value = AuthState.Authenticated(updatedUser)
                } else {
                    repository.dao.clearRememberedUsers()
                    val updatedUser = user.copy(isRemembered = false)
                    repository.dao.insertUser(updatedUser)
                    _authState.value = AuthState.Authenticated(updatedUser)
                }
            } else {
                _authState.value = AuthState.Error("Incorrect password. Please try again.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.dao.clearRememberedUsers()
            _authState.value = AuthState.Idle
        }
    }

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            val user = repository.dao.getUserByEmail(email)
            if (user != null) {
                _verificationMessage.value = "A secure password reset verification link has been dispatched to $email. Please check your inbox."
            } else {
                _verificationMessage.value = "We couldn't locate any account registered with $email."
            }
        }
    }

    fun requestEmailVerification(email: String) {
        viewModelScope.launch {
            _verificationMessage.value = "An email verification code has been dispatched to $email. Please enter it to verify your registration."
        }
    }

    fun clearVerificationMessage() {
        _verificationMessage.value = null
    }

    fun updateProfile(name: String, email: String, passwordHash: String?) {
        viewModelScope.launch {
            val currentState = _authState.value
            if (currentState is AuthState.Authenticated) {
                val updatedUser = currentState.user.copy(
                    name = name,
                    email = email,
                    passwordHash = passwordHash ?: currentState.user.passwordHash
                )
                repository.dao.insertUser(updatedUser)
                _authState.value = AuthState.Authenticated(updatedUser)
            }
        }
    }
}
