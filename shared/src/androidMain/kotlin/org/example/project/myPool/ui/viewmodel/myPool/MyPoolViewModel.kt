package org.example.project.myPool.ui.viewmodel.myPool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.ui.logic.myPoolLogic.MyPoolLogic

class MyPoolViewModel(
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
) : ViewModel() {

    val logic = MyPoolLogic(getMyOrders, computeDistancesUseCase, viewModelScope)

    fun updateDeviceLocation(lat: Double, lng: Double) {
        logic.updateDeviceLocation(Coordinates(lat, lng))
    }

    override fun onCleared() {
        logic.clear()
        super.onCleared()
    }
}