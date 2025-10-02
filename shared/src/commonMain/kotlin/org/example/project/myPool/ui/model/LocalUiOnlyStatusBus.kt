package org.example.project.myPool.ui.model

import kotlinx.coroutines.flow.MutableSharedFlow
import org.example.project.myPool.domian.model.OrderStatus

object LocalUiOnlyStatusBus {
    // Emits `(orderId, newStatus)` to reflect a local status change in the UI
    val statusEvents = MutableSharedFlow<Pair<String, OrderStatus>>(extraBufferCapacity = 8)

    // Emits `(message, retryAction)` for transient UI errors.
    val errorEvents = MutableSharedFlow<Pair<String, (() -> Unit)?>>(extraBufferCapacity = 8)
}