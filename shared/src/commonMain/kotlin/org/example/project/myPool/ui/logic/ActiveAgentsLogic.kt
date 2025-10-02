package org.example.project.myPool.ui.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.myPool.domian.model.ActiveUser
import org.example.project.myPool.domian.usecase.GetActiveUsersUseCase
import org.example.project.myPool.ui.model.AgentsState

class ActiveAgentsLogic(
    private val getActiveUsers: GetActiveUsersUseCase,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(AgentsState())
    val state: StateFlow<AgentsState> = _state

    fun load() {
        if (_state.value.isLoading) return
        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            try {
                val (list, currentId) = getActiveUsers()
                _state.value = AgentsState(
                    isLoading = false,
                    agents = list,
                    currentUserId = currentId
                )
            } catch (e: Exception) { // use Exception instead of IOException for KMP safety
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed")
            }
        }
    }
}