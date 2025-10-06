package org.example.project.myPool.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.UserStore
import org.example.project.location.domain.model.ComputeDistancesUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.ui.state.MyOrdersUiState

class MyOrdersViewModel(
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
    private val userStore: UserStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyOrdersUiState(isLoading = false))
    val uiState: StateFlow<MyOrdersUiState> = _uiState.asStateFlow()

    private val currentUserId = MutableStateFlow<String?>(userStore.toString())
    private val deviceLocation = MutableStateFlow<Location?>(null)

    // Shared store across sub-ViewModels
    private val store =
        OrdersStore(
            state = _uiState,
            currentUserId = currentUserId,
            deviceLocation = deviceLocation,
            allOrders = mutableListOf(),
        )

    val listVM = OrdersListViewModel(store, getMyOrders, computeDistancesUseCase)
    val searchVM = OrdersSearchViewModel(store)
    val statusVM = OrdersStatusViewModel(store)

    init {
        listVM.refreshOrders()
    }
}
