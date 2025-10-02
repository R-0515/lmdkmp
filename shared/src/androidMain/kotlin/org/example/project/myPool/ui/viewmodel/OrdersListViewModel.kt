package org.example.project.myPool.ui.viewmodel

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.ui.logic.OrdersListLogic
import org.example.project.myPool.ui.state.OrdersStore


class OrdersListViewModel(
    private val store: OrdersStore,
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
) : ViewModel() {

    val logic = OrdersListLogic(store, getMyOrders, computeDistancesUseCase, viewModelScope)

    fun updateDeviceLocation(location: Location?) {
        logic.updateDeviceLocation(
            location?.let { Coordinates(it.latitude, it.longitude) }
        )
    }

    fun loadOrders(context: Context) = logic.loadInitial()
    fun retry(context: Context) = logic.retry()
    fun refresh(context: Context) = logic.refresh()
    fun refreshOrders() = logic.refreshOrders()
    fun loadNextPage(context: Context) = logic.loadNextPage()
}