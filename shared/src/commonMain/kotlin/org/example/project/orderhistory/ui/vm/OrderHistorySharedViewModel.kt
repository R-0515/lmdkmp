package org.example.project.orderhistory.ui.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.orderhistory.domain.model.OrderHistoryStatus
import org.example.project.orderhistory.domain.model.OrderHistoryUi
import org.example.project.orderhistory.domain.model.OrdersHistoryFilter
import org.example.project.orderhistory.domain.usecase.GetOrdersUseCase

class OrderHistorySharedViewModel(
    private val getOrdersUseCase: GetOrdersUseCase,
) {
    companion object { private const val PREFETCH_THRESHOLD = 3 }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ---------- UI state ----------
    private val _orders = MutableStateFlow<List<OrderHistoryUi>>(emptyList())
    val orders: StateFlow<List<OrderHistoryUi>> = _orders.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _endReached = MutableStateFlow(false)
    val endReached: StateFlow<Boolean> = _endReached.asStateFlow()


    // ---------- filter state ----------
    private val _filter = MutableStateFlow(OrdersHistoryFilter())
    val filter: StateFlow<OrdersHistoryFilter> = _filter.asStateFlow()

    private var currentPage = 1
    private val pageSize = 20

    // ---------- public intents ----------
    fun loadOrders() = scope.launch {
        _isRefreshing.value = true
        _endReached.value = false
        currentPage = 1

        val paged = runCatching { getOrdersUseCase(page = currentPage, limit = pageSize) }
            .getOrElse {
                _orders.value = emptyList()
                _isRefreshing.value = false
                _endReached.value = true
                return@launch
            }

        val filtered = paged.items
            .filterByStatus(_filter.value.allowed)
            .applySorting(_filter.value.ageAscending)

        _orders.value = filtered
        _endReached.value = filtered.isEmpty()
        _isRefreshing.value = false
    }

    fun refreshOrders() {
        if (_isRefreshing.value) return
        loadOrders()
    }

    fun loadMoreIfNeeded(lastVisibleIndex: Int) {
        if (_endReached.value || _isLoadingMore.value) return
        if (lastVisibleIndex >= _orders.value.size - PREFETCH_THRESHOLD) {
            loadMoreOrders()
        }
    }

    fun loadMoreOrders() {
        if (_isLoadingMore.value || _endReached.value) return
        scope.launch {
            _isLoadingMore.value = true
            currentPage++

            val page = runCatching { getOrdersUseCase(page = currentPage, limit = pageSize) }
                .map { it.items.filterByStatus(_filter.value.allowed) }
                .getOrElse { emptyList() }

            if (page.isEmpty()) {
                _endReached.value = true
            } else {
                _orders.value = _orders.value + page
            }
            _isLoadingMore.value = false
        }
    }

    fun setAllowedStatuses(statuses: Set<OrderHistoryStatus>) {
        _filter.value = _filter.value.copy(allowed = statuses)
        loadOrders()
    }

    fun setAgeAscending(ascending: Boolean) {
        _filter.value = _filter.value.copy(ageAscending = ascending)
        _orders.value = _orders.value.applySorting(ascending)
    }

    fun resetFilters() {
        _filter.value = OrdersHistoryFilter()
        currentPage = 1
        _endReached.value = false
    }

    fun clear() { scope.cancel() }
}

private fun List<OrderHistoryUi>.filterByStatus(
    allowed: Set<OrderHistoryStatus>
): List<OrderHistoryUi> =
    if (allowed.isEmpty()) this else filter { it.status in allowed }

private fun List<OrderHistoryUi>.applySorting(ageAscending: Boolean): List<OrderHistoryUi> =
    if (ageAscending) sortedBy { it.createdAtMillis } else sortedByDescending { it.createdAtMillis }

object OrdersPaging { const val PAGE_SIZE = 20 }
