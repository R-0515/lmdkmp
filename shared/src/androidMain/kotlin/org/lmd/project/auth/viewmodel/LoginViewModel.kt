package org.lmd.project.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.lmd.project.auth.domain.usecase.LoginUseCase
import org.lmd.project.core.utils.ApiResult

actual class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    actual val uiState: StateFlow<LoginUiState> = _uiState

    actual fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = loginUseCase(email, password)) {
                is ApiResult.Success -> _uiState.value = LoginUiState.Success(result.data)
                is ApiResult.Error -> _uiState.value = LoginUiState.Error(result.message)
            }
        }
    }
}

