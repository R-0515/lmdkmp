package org.example.project.myPool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okio.IOException
import org.example.project.myPool.domian.usecase.GetActiveUsersUseCase
import org.example.project.myPool.ui.model.AgentsState

class ActiveAgentsViewModel(
    private val getActiveUsers: GetActiveUsersUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(AgentsState())
    val state: StateFlow<AgentsState> = _state

    fun load() {
        if (_state.value.isLoading) return
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val (list, currentId) = getActiveUsers()
                _state.value =
                    AgentsState(
                        isLoading = false,
                        agents = list,
                        currentUserId = currentId,
                    )
            } catch (e: IOException) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed")
            }
        }
    }
}
