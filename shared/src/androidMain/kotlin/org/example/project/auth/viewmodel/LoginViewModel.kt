package org.example.project.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.auth.data.model.LoginData
import org.example.project.auth.domain.usecase.LoginUseCase
import org.example.project.core.utils.ApiResult

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val data: LoginData) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = loginUseCase(email, password)) {
                is ApiResult.Success -> {
                    _uiState.value = LoginUiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                }
            }
        }
    }
}
