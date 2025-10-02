package org.example.project.myPool.ui.logic

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.domian.model.toApiId
import org.example.project.myPool.domian.usecase.UpdateOrderStatusUseCase


class UpdateOrderStatusLogic(
    private val updateStatus: UpdateOrderStatusUseCase,
    private val userStore: SecureUserStore,
    private val scope: CoroutineScope
) {

    private val _updatingIds = MutableStateFlow<Set<String>>(emptySet())
    val updatingIds: StateFlow<Set<String>> = _updatingIds

    private val _success = MutableSharedFlow<OrderInfo>()
    val success: SharedFlow<OrderInfo> = _success

    private val _currentUserId = MutableStateFlow<String?>(userStore.getUserId())
    val currentUserId: StateFlow<String?> = _currentUserId

    private val _error = MutableSharedFlow<Pair<String, () -> Unit>>()
    val error: SharedFlow<Pair<String, () -> Unit>> = _error

    init {
        userStore.onUserChanged = { id -> _currentUserId.value = id }
    }

    fun update(orderId: String, targetStatus: OrderStatus, assignedAgentId: String? = null) {
        if (targetStatus == OrderStatus.REASSIGNED && assignedAgentId.isNullOrBlank()) {
            scope.launch {
                _error.emit("Missing assignee for REASSIGNED" to { /* no-op */ })
            }
            return
        }

        _updatingIds.update { it + orderId }
        scope.launch {
            try {
                runCatching { updateStatus(orderId, targetStatus.toApiId(), assignedAgentId) }
                    .onSuccess { serverOrder -> _success.emit(serverOrder) }
                    .onFailure { e ->
                        if (e is CancellationException) throw e
                        _error.emit(
                            e.message.orEmpty() to { update(orderId, targetStatus, assignedAgentId) }
                        )
                    }
            } finally {
                _updatingIds.update { it - orderId }
            }
        }
    }
}