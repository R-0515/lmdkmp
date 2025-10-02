package org.example.project.myPool.ui.viewmodel

import android.content.Context
import org.example.project.myPool.ui.logic.myOrderLogic.OrdersErrorCore
import org.example.project.myPool.ui.model.LocalUiOnlyStatusBus
import org.example.project.util.toUserMessage
import java.io.IOException

class OrdersListErrorHandler(
    private val core: OrdersErrorCore
) {
    fun handleInitialError(
        e: IOException,
        alreadyHasData: Boolean,
        context: Context,
        retryInitial: (Context) -> Unit
    ) {
        val msg = e.toUserMessage()
        core.handleInitialErrorMessage(msg, alreadyHasData) { retryInitial(context) }

        // UI-only event bus
        LocalUiOnlyStatusBus.errorEvents.tryEmit(msg to { retryInitial(context) })
    }

    fun postError(message: String, retry: () -> Unit) {
        core.postError(message, retry)
        LocalUiOnlyStatusBus.errorEvents.tryEmit(message to retry)
    }
}