package org.example.project.myPool.ui.viewmodel.myOrder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.ui.logic.myOrderLogic.MyOrdersLogic

class MyOrdersViewModel(
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
    userStore: SecureUserStore
) : ViewModel() {

    private val currentUserId = MutableStateFlow<String?>(userStore.getUserId())
    private val deviceLocation = MutableStateFlow<Coordinates?>(null)

    val logic = MyOrdersLogic(
        getMyOrders = getMyOrders,
        computeDistancesUseCase = computeDistancesUseCase,
        currentUserId = currentUserId,
        deviceLocation = deviceLocation,
        scope = viewModelScope
    )

    val uiState get() = logic.uiState
    val listVM get() = logic.listLogic
    val searchVM get() = logic.searchLogic
    val statusVM get() = logic.statusLogic
}