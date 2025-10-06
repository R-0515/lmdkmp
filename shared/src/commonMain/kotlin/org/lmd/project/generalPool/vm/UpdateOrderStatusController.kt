package org.lmd.project.generalPool.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.lmd.project.generalPool.domain.model.OrderInfo
import org.lmd.project.generalPool.domain.model.OrderStatus
import org.lmd.project.generalPool.domain.model.toApiId
import org.lmd.project.generalPool.domain.usecase.UpdateOrderStatusUseCase
import kotlin.coroutines.cancellation.CancellationException

class UpdateOrderStatusController(
    private val updateStatus: UpdateOrderStatusUseCase,
    private val scope: CoroutineScope
) {
    private val _updatingIds = MutableStateFlow<Set<String>>(emptySet())
    val updatingIds: StateFlow<Set<String>> = _updatingIds

    // One-shot events: small buffer so late collectors don't miss quick emissions
    private val _success = MutableSharedFlow<OrderInfo>(
        replay = 0, extraBufferCapacity = 1
    )
    val success: SharedFlow<OrderInfo> = _success

    data class RetryEvent(
        val message: String,
        val orderId: String,
        val targetStatus: OrderStatus,
        val assignedAgentId: String?
    )
    private val _error = MutableSharedFlow<RetryEvent>(
        replay = 0, extraBufferCapacity = 1
    )
    val error: SharedFlow<RetryEvent> = _error

    fun update(orderId: String, targetStatus: OrderStatus, assignedAgentId: String? = null) {
        if (targetStatus == OrderStatus.REASSIGNED && assignedAgentId.isNullOrBlank()) {
            scope.launch {
                _error.emit(RetryEvent("Missing assignee for REASSIGNED", orderId, targetStatus, assignedAgentId))
            }
            return
        }

        // Guard: if already updating this order, ignore this tap
        if (orderId in _updatingIds.value) return

        _updatingIds.update { it + orderId }
        scope.launch {
            try {
                println("API start | orderId=$orderId target=$targetStatus")
                runCatching { updateStatus(orderId, targetStatus.toApiId(), assignedAgentId) }
                    .onSuccess { serverOrder ->
                        println("API success | orderId=$orderId new=${serverOrder.status}")
                        _success.emit(serverOrder)
                    }
                    .onFailure { e ->
                        if (e is CancellationException) throw e
                        println("API error | orderId=$orderId target=$targetStatus msg=${e.message}")
                        _error.emit(RetryEvent(e.toUserMessage(), orderId, targetStatus, assignedAgentId))
                    }
            } finally {
                _updatingIds.update { it - orderId }
            }
        }
    }

    fun isUpdating(orderId: String): Boolean = orderId in _updatingIds.value

    fun clear() {
        scope.cancel() // safe to call even if scope is managed by the platform
    }
}

fun Throwable.toUserMessage(): String = message ?: "Something went wrong"
