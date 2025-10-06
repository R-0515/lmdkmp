package org.example.project.myPool.ui.viewmodel

import android.content.Context
import kotlinx.coroutines.flow.update
import org.example.project.myPool.ui.logic.OrdersStore
import org.example.project.myPool.ui.model.LocalUiOnlyStatusBus
import org.example.project.util.toUserMessage
import java.io.IOException

class OrdersListErrorHandler(
    private val store: OrdersStore,
    private val helpers: OrdersListHelpersAndroid,
) {
    private val state get() = store.state

    fun handleInitialError(
        e: IOException,
        alreadyHasData: Boolean,
        context: Context,
        retryInitial: (Context) -> Unit,
    ) {
        val msg = e.toUserMessage()
        if (alreadyHasData) {
            state.update { it.copy(isLoading = false, errorMessage = msg) }
            LocalUiOnlyStatusBus.errorEvents.tryEmit(msg to { retryInitial(context) })
        } else {
            helpers.handleInitialLoadError(
                e = e,
                alreadyHasData = alreadyHasData,
                context = context,
                state = state,
                retry = retryInitial,
            )
        }
    }

    fun postError(
        message: String,
        retry: () -> Unit,
    ) {
        state.update { it.copy(errorMessage = message) }
        LocalUiOnlyStatusBus.errorEvents.tryEmit(message to retry)
    }
}
