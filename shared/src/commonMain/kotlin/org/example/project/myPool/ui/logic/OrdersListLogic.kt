package org.example.project.myPool.ui.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.ui.logic.myOrderLogic.OrdersListPublisher
import org.example.project.myPool.ui.logic.myOrderLogic.OrdersPager
import org.example.project.myPool.ui.logic.myOrderLogic.OrdersThrottle
import org.example.project.myPool.ui.model.OrdersListControllerDeps
import org.example.project.myPool.ui.state.OrdersStore


class OrdersListLogic(
    private val store: OrdersStore,
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
    private val scope: CoroutineScope
) {
    private val helpers = OrdersListHelpers(store, computeDistancesUseCase)
    private val pager = OrdersPager(getMyOrders)
    private val publisher = OrdersListPublisher(store, helpers)
    private val errors = OrdersListErrorHandler(store, helpers)
    private val throttle = OrdersThrottle()

    private val controller =
        OrdersListController(
            deps = OrdersListControllerDeps(
                store = store,
                pager = pager,
                publisher = publisher,
                errors = errors,
                throttle = throttle
            ),
            scope = scope
        )

    fun setCurrentUserId(id: String?) = controller.setCurrentUserId(id)

    fun refreshOrders() = controller.refreshStrict()

    fun loadNextPage() = controller.loadNextPage()

    fun refresh() = controller.refresh()

    fun retry() = controller.loadInitial()

    fun loadInitial() = controller.loadInitial()

    fun updateDeviceLocation(location: Coordinates?) {
        store.deviceLocation.value = location
        if (location != null && store.state.value.orders.isNotEmpty()) {
            val computed = helpers.withDistances(location, store.state.value.orders)
            store.state.update { it.copy(orders = computed) }
        }
    }
}