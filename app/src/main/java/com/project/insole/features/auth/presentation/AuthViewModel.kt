package com.project.insole.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.insole.core.network.SessionManager
import com.project.insole.features.auth.domain.LoginUseCase
import com.project.insole.features.auth.domain.LogoutUseCase
import com.project.insole.features.auth.domain.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel for authentication screens.
 * Exposes immutable StateFlow for UI consumption.
 * Only accesses domain use cases and session storage.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            val result = loginUseCase(email, password)
            result.onSuccess { auth ->
                sessionManager.saveSession(auth.token, auth.role, auth.pseudoId)
                _authState.value = AuthUiState(isLoggedIn = true)
            }.onFailure { exception ->
                _authState.value = AuthUiState(errorMessage = exception.message ?: "Authentication failed")
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String = "") {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            val result = signUpUseCase(email, password, displayName)
            result.onSuccess { auth ->
                sessionManager.saveSession(auth.token, auth.role, auth.pseudoId)
                _authState.value = AuthUiState(isLoggedIn = true)
            }.onFailure { exception ->
                _authState.value = AuthUiState(errorMessage = exception.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true)
            logoutUseCase()
            _authState.value = AuthUiState(isLoggedIn = false)
        }
    }
}
