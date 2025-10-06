package org.example.project.myPool.ui.logic


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.UserStore
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.domian.model.toApiId
import org.example.project.myPool.domian.usecase.UpdateOrderStatusUseCase
import org.example.project.util.toUserMessage
import kotlin.coroutines.cancellation.CancellationException

class UpdateOrderStatusLogic(
    private val updateStatus: UpdateOrderStatusUseCase,
    private val userStore: UserStore,
    private val scope: CoroutineScope,
    private val log: OrderLogger = OrderLogger(), // injected platform logger
) {
    private val _updatingIds = MutableStateFlow<Set<String>>(emptySet())
    val updatingIds: StateFlow<Set<String>> get() = _updatingIds

    private val _success = MutableSharedFlow<OrderInfo>()
    val success: SharedFlow<OrderInfo> get() = _success

    private val _error = MutableSharedFlow<Pair<String, () -> Unit>>()
    val error: SharedFlow<Pair<String, () -> Unit>> get() = _error

    private val _currentUserId = MutableStateFlow<String?>(userStore.getUserId())
    val currentUserId: StateFlow<String?> get() = _currentUserId

    fun update(
        orderId: String,
        targetStatus: OrderStatus,
        assignedAgentId: String? = null,
    ) {
        if (targetStatus == OrderStatus.REASSIGNED && assignedAgentId.isNullOrBlank()) {
            scope.launch { _error.emit("Missing assignee for REASSIGNED" to { }) }
            return
        }

        _updatingIds.update { it + orderId }

        scope.launch {
            try {
                log.postStart(orderId, targetStatus)
                runCatching {
                    updateStatus(orderId, targetStatus.toApiId(), assignedAgentId)
                }.onSuccess { serverOrder ->
                    log.postSuccess(orderId, serverOrder.status)
                    _success.emit(serverOrder)
                }.onFailure { e ->
                    if (e is CancellationException) throw e
                    log.postError(orderId, targetStatus, e)
                    _error.emit(e.toUserMessage() to { update(orderId, targetStatus, assignedAgentId) })
                }
            } finally {
                _updatingIds.update { it - orderId }
            }
        }
    }
}

/**
 * Platform-agnostic logger interface for multiplatform logging.
 */
expect class OrderLogger() {
    fun uiTap(orderId: String, orderNumber: String?, action: String)
    fun postStart(orderId: String, target: OrderStatus)
    fun postSuccess(orderId: String, new: OrderStatus?)
    fun postError(orderId: String, target: OrderStatus, t: Throwable)
}