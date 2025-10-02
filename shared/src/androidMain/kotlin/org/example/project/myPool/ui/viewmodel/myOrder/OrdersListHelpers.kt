package org.example.project.myPool.ui.viewmodel.myOrder

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.location.domain.model.Coordinates
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.ui.logic.myOrderLogic.OrdersListHelpersCore
import org.example.project.myPool.ui.model.LocalUiOnlyStatusBus
import org.example.project.myPool.ui.state.MyOrdersUiState

class OrdersListHelpers(
    private val core: OrdersListHelpersCore
) {
    fun handlePagingError(
        msg: String,
        context: Context,
        state: MutableStateFlow<MyOrdersUiState>,
        retry: (Context) -> Unit,
    ) {
        LocalUiOnlyStatusBus.errorEvents.tryEmit(msg to { retry(context) })
        state.update { it.copy(isLoadingMore = false) }
    }

    fun handleInitialLoadError(
        e: Exception,
        alreadyHasData: Boolean,
        context: Context,
        state: MutableStateFlow<MyOrdersUiState>,
        retry: (Context) -> Unit,
    ) {
        val msg = messageFor(e)
        state.update {
            it.copy(
                isLoading = false,
                errorMessage = if (!alreadyHasData) msg else null,
            )
        }
        if (alreadyHasData) {
            LocalUiOnlyStatusBus.errorEvents.tryEmit(msg to { retry(context) })
        }
    }

    fun messageFor(e: Exception): String =
        when (e) {
            is HttpException -> "HTTP ${e.code()}"
            else -> core.messageFor(e)
        }

    // expose common functions
    fun computeDisplay(coords: Coordinates?, source: List<OrderInfo>, query: String?, uid: String?) =
        core.computeDisplay(coords, source, query, uid)

    fun withDistances(coords: Coordinates?, list: List<OrderInfo>) =
        core.withDistances(coords, list)

    fun publishFirstPageFrom(state: MutableStateFlow<MyOrdersUiState>, base: List<OrderInfo>, pageSize: Int, query: String, endReached: Boolean) =
        core.publishFirstPageFrom(state, base, pageSize, query, endReached)

    fun publishAppendFrom(state: MutableStateFlow<MyOrdersUiState>, base: List<OrderInfo>, page: Int, pageSize: Int, endReached: Boolean) =
        core.publishAppendFrom(state, base, page, pageSize, endReached)

    fun applyDisplayFilter(list: List<OrderInfo>, query: String, currentUserId: String?) =
        core.applyDisplayFilter(list, query, currentUserId)
}