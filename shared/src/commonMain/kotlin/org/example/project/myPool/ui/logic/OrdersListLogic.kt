package org.example.project.myPool.ui.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import org.example.project.myPool.domian.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.ui.state.MyOrdersUiState
import org.example.project.socket.Coordinates

/**
 * Shared OrdersListLogic for both Android & iOS.
 * Contains no Android imports or Context references.
 */
class OrdersListLogic(
    private val store: OrdersStore,
    getMyOrders: GetMyOrdersUseCase,
    private val computeDistancesUseCase: ComputeDistancesUseCase,
    private val scope: CoroutineScope,
    private val helpers: OrdersListHelpersCommon = OrdersListHelpersCommon(computeDistancesUseCase),
) {

    private val pager = OrdersPager(getMyOrders)
    private val publisher = OrdersListPublisher(store, helpers)
    private val throttle = OrdersThrottle()

    private val deps = OrdersListControllerDeps(
        store = store,
        pager = pager,
        publisher = publisher,
        throttle = throttle,
        //errors = TODO(),
    )

    private val controller = OrdersListController(
        deps = deps,
        scope = scope,
        onError = { message, retry ->
            store.state.update { it.copy(errorMessage = message) }
            println("⚠️ Error: $message (retry available)")
        }
    )

    fun updateDeviceLocation(coordinates: Coordinates?) {
        store.deviceLocation.value = coordinates
        if (coordinates != null && store.state.value.orders.isNotEmpty()) {
            val computed = helpers.withDistances(coordinates, store.state.value.orders)
            store.state.update { it.copy(orders = computed) }
        }
    }

    fun setCurrentUserId(id: String?) = controller.setCurrentUserId(id)
    fun refreshOrders() = controller.refreshStrict()
    fun loadOrders() = controller.loadInitial()
    fun loadNextPage() = controller.loadNextPage()

    // Optional simple helpers for iOS
    fun getUiState(): MyOrdersUiState = store.state.value
}
