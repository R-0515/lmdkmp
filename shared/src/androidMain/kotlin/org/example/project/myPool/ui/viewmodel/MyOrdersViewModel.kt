package org.example.project.myPool.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import org.example.project.UserStore
import org.example.project.myPool.domian.mapper.toCoordinates
import org.example.project.myPool.domian.usecase.ComputeDistancesUseCase
import org.example.project.socket.Coordinates
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.ui.logic.MyOrdersLogic

class MyOrdersViewModel(
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
    userStore: UserStore,
) : ViewModel() {

    private val currentUserId = MutableStateFlow<String?>(userStore.getUserId())
    private val deviceLocation = MutableStateFlow<Coordinates?>(null)

    private val logic = MyOrdersLogic(
        getMyOrders = getMyOrders,
        computeDistancesUseCase = computeDistancesUseCase,
        currentUserId = currentUserId,
        deviceLocation = deviceLocation,
        scope = viewModelScope,
    )

    val uiState = logic.uiState
    val listVM = logic.listVM
    val searchVM = logic.searchVM
    val statusVM = logic.statusVM

    fun updateDeviceLocation(location: Location?) {
        deviceLocation.value = location?.toCoordinates()
    }
}