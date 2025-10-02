package org.example.project.myPool.ui.logic.myOrderLogic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.ui.state.MyOrdersUiState
import org.example.project.myPool.ui.state.OrdersStore

class MyOrdersLogic(
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
    private val currentUserId: MutableStateFlow<String?>,
    private val deviceLocation: MutableStateFlow<Coordinates?>,
    scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(MyOrdersUiState(isLoading = false))
    val uiState: StateFlow<MyOrdersUiState> = _uiState.asStateFlow()

    // Shared store across sub-VMs
    private val store =
        OrdersStore(
            state = _uiState,
            currentUserId = currentUserId,
            deviceLocation = deviceLocation,
            allOrders = mutableListOf(),
        )

    val listLogic = OrdersListViewModel(store, getMyOrders, computeDistancesUseCase)
    val searchLogic = OrdersSearchViewModel(store)
    val statusLogic = OrdersStatusViewModel(store)

    init {
        listLogic.refreshOrders()
    }
}