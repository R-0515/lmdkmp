package org.example.project.myPool.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import org.example.project.myPool.domian.mapper.toCoordinates
import org.example.project.myPool.domian.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.ui.logic.*

/**
 * Android ViewModel wrapper that connects the shared logic with Android Context and lifecycle.
 */

class OrdersListViewModel(
    private val store: OrdersStore,
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
) : ViewModel() {

    private val commonHelpers = OrdersListHelpersCommon(computeDistancesUseCase)
    private val helpers = OrdersListHelpersAndroid(commonHelpers)
    private val pager = OrdersPager(getMyOrders)
    private val publisher = OrdersListPublisher(store, commonHelpers)
    private val errors = OrdersListErrorHandler(store, helpers)
    private val throttle = OrdersThrottle()

    private val controller = OrdersListController(
        deps = OrdersListControllerDeps(
            store = store,
            pager = pager,
            publisher = publisher,
            throttle = throttle,
        ),
        scope = viewModelScope,
        onError = { msg, retry -> errors.postError(msg, retry) },
    )

    fun setCurrentUserId(id: String?) = controller.setCurrentUserId(id)
    fun loadOrders() = controller.refreshStrict()
    fun refreshOrders() = controller.refreshStrict()
    fun loadNextPage() = controller.loadNextPage()

    fun updateDeviceLocation(location: Location?) {
        val coords = location?.toCoordinates()
        store.deviceLocation.value = coords
        if (coords != null && store.state.value.orders.isNotEmpty()) {
            val computed = commonHelpers.withDistances(coords, store.state.value.orders)
            store.state.update { it.copy(orders = computed) }
        }
    }
}