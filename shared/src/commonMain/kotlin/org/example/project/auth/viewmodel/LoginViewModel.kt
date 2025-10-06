package org.example.project.auth.viewmodel


import kotlinx.coroutines.flow.StateFlow

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val data: Any) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

expect class LoginViewModel {
    val uiState: StateFlow<LoginUiState>
    fun login(email: String, password: String)
}
