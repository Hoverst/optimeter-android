package com.optimeter.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.optimeter.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    val isUserLoggedIn = authRepository.isUserLoggedIn

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun toggleMode() {
        val currentMode = _uiState.value.isLoginMode
        _uiState.value = _uiState.value.copy(isLoginMode = !currentMode, error = null)
    }

    fun submit() {
        val email = _uiState.value.email
        val password = _uiState.value.password
        val isLogin = _uiState.value.isLoginMode

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email and password cannot be empty")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = if (isLogin) {
                authRepository.signInWithEmail(email, password)
            } else {
                authRepository.registerWithEmail(email, password)
            }

            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Authentication failed"
                )
            }
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.signInWithGoogleIdToken(idToken)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Google sign-in failed"
                )
            }
        }
    }
}

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)
