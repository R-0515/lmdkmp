package org.example.project.orderhistory

import androidx.lifecycle.ViewModel
import org.example.project.orderhistory.domain.model.OrderHistoryStatus
import org.example.project.orderhistory.domain.usecase.GetOrdersUseCase
import org.example.project.orderhistory.ui.vm.OrderHistorySharedViewModel


class OrderHistoryViewModel(
    getOrdersUseCase: GetOrdersUseCase
) : ViewModel() {

    private val shared = OrderHistorySharedViewModel(getOrdersUseCase)

    val orders = shared.orders
    val isRefreshing = shared.isRefreshing
    val isLoadingMore = shared.isLoadingMore
    val endReached = shared.endReached
    val filter = shared.filter

    fun loadOrders() = shared.loadOrders()
    fun loadMoreOrders() = shared.loadMoreOrders()
    fun setAllowedStatuses(statuses: Set<OrderHistoryStatus>) =
        shared.setAllowedStatuses(statuses)
    fun loadMoreIfNeeded(lastVisibleIndex: Int) = shared.loadMoreIfNeeded(lastVisibleIndex)
    fun refreshOrders() = shared.refreshOrders()
    fun setAgeAscending(ascending: Boolean) = shared.setAgeAscending(ascending)
    fun resetFilters() = shared.resetFilters()

    override fun onCleared() {
        super.onCleared()
        shared.clear()
    }
}
