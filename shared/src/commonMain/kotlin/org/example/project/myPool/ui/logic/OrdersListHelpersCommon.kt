package org.example.project.myPool.ui.logic

import org.example.project.socket.Coordinates
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.domian.usecase.ComputeDistancesUseCase
import org.example.project.myPool.ui.state.MyOrdersUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.min

/**
 * Common logic for filtering, sorting, distance computation, and UI state publishing.
 * Safe for both Android and iOS.
 */
class OrdersListHelpersCommon(
    private val computeDistancesUseCase: ComputeDistancesUseCase,
) {
    private val allowedStatuses =
        setOf(
            OrderStatus.ADDED,
            OrderStatus.CONFIRMED,
            OrderStatus.REASSIGNED,
            OrderStatus.CANCELED,
            OrderStatus.PICKUP,
            OrderStatus.START_DELIVERY,
        )

    fun applyDisplayFilter(
        list: List<OrderInfo>,
        query: String,
        currentUserId: String?,
    ): List<OrderInfo> {
        val q = query.trim()
        val afterQuery =
            if (q.isBlank()) list else list.filter { o ->
                o.orderNumber.contains(q, ignoreCase = true) ||
                        o.name.contains(q, ignoreCase = true) ||
                        (o.details?.contains(q, ignoreCase = true) == true)
            }
        val afterStatus = afterQuery.filter { it.status in allowedStatuses }
        return if (currentUserId.isNullOrBlank()) afterStatus
        else afterStatus.filter { it.assignedAgentId == currentUserId }
    }

    fun computeDisplay(
        coordinates: Coordinates?, // platform-specific coordinate model (handled on Android)
        source: List<OrderInfo>,
        query: String?,
        uid: String?,
    ): List<OrderInfo> {
        val filtered = applyDisplayFilter(source, query.orEmpty(), uid)
        return withDistances(coordinates, filtered)
    }

    fun withDistances(
        coordinates: Coordinates?,
        list: List<OrderInfo>,
    ): List<OrderInfo> =
        if (coordinates != null ) {
            computeDistancesUseCase(coordinates, list)
        } else list

    fun publishFirstPageFrom(
        state: MutableStateFlow<MyOrdersUiState>,
        base: List<OrderInfo>,
        pageSize: Int,
        query: String,
        endReached: Boolean,
    ) {
        val first = base.take(pageSize)
        val emptyMsg =
            when {
                base.isEmpty() && query.isBlank() -> "No active orders."
                base.isEmpty() && query.isNotBlank() -> "No matching orders."
                else -> null
            }
        state.update {
            it.copy(
                isLoading = false,
                isLoadingMore = false,
                orders = first,
                emptyMessage = emptyMsg,
                errorMessage = null,
                page = 1,
                endReached = endReached,
            )
        }
    }

    fun publishAppendFrom(
        state: MutableStateFlow<MyOrdersUiState>,
        base: List<OrderInfo>,
        page: Int,
        pageSize: Int,
        endReached: Boolean,
    ) {
        val visibleCount = min(page * pageSize, base.size)
        state.update {
            it.copy(
                isLoadingMore = false,
                orders = base.take(visibleCount),
                endReached = endReached,
            )
        }
    }

    fun messageFor(e: Exception): String =
        e.message ?: "Unknown error"
}

/**
 * Interface used to abstract Location between Android/iOS
 */
interface LocationLike {
    val latitude: Double
    val longitude: Double
}
