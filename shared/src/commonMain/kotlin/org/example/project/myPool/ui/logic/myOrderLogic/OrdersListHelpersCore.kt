package org.example.project.myPool.ui.logic.myOrderLogic

import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus


class OrdersListHelpersCore(
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
            if (q.isBlank()) list
            else list.filter { o ->
                o.orderNumber.contains(q, ignoreCase = true) ||
                        o.name.contains(q, ignoreCase = true) ||
                        (o.details?.contains(q, ignoreCase = true) == true)
            }

        val afterStatus = afterQuery.filter { it.status in allowedStatuses }

        return if (currentUserId.isNullOrBlank()) afterStatus
        else afterStatus.filter { it.assignedAgentId == currentUserId }
    }

    fun computeDisplay(
        coords: Coordinates?,
        source: List<OrderInfo>,
        query: String?,
        uid: String?,
    ): List<OrderInfo> {
        val filtered = applyDisplayFilter(source, query.orEmpty(), uid)
        return withDistances(coords, filtered)
    }

    fun withDistances(
        coords: Coordinates?,
        list: List<OrderInfo>,
    ): List<OrderInfo> {
        if (coords == null) return list
        val targets = list.map { Coordinates(it.lat, it.lng) }
        val distances = computeDistancesUseCase.computeDistances(coords, targets)
        return list.zip(distances) { order, dist -> order.copy(distanceKm = dist) }
            .sortedBy { it.distanceKm }
    }

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

    fun messageFor(e: Throwable): String =
        when (e) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Request timed out"
            is java.io.IOException -> "Network error"
            else -> e.message ?: "Unknown error"
        }
}