package org.example.project.myPool.ui.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.myPool.domian.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.ui.state.MyOrdersUiState
import org.example.project.socket.Coordinates

class MyOrdersLogic(
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
    private val currentUserId: MutableStateFlow<String?>,
    private val deviceLocation: MutableStateFlow<Coordinates?>,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow(MyOrdersUiState(isLoading = false))
    val uiState = _uiState

    private val store = OrdersStore(
        state = _uiState,
        currentUserId = currentUserId,
        deviceLocation = deviceLocation,
        allOrders = mutableListOf(),
    )

    val listVM = OrdersListLogic(store, getMyOrders, computeDistancesUseCase, scope)
    val searchVM = OrdersSearchLogic(store)
    val statusVM = OrdersStatusLogic(store)

    init {
        listVM.refreshOrders()
    }
}