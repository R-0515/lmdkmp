package org.example.project.myPool.ui.viewmodel

import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.myPool.domian.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.domian.util.OrdersPaging
import org.example.project.myPool.ui.logic.MyPoolLogic
import org.example.project.myPool.ui.model.MyOrdersPoolUiState
import org.example.project.myPool.ui.model.MyPoolLoadResult
import org.example.project.socket.Coordinates
import java.io.IOException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

class MyPoolViewModel(
    getMyOrders: GetMyOrdersUseCase,
    computeDistancesUseCase: ComputeDistancesUseCase,
) : ViewModel() {

    private val deviceLocation = MutableStateFlow<Location?>(null)
    private val sharedLocation = MutableStateFlow<Coordinates?>(null)

    private val logic = MyPoolLogic(
        getMyOrders = getMyOrders,
        computeDistancesUseCase = computeDistancesUseCase,
        deviceLocation = sharedLocation,
        scope = viewModelScope,
    )

    val ui = logic.ui
    val lastLocation = deviceLocation

    init {
        viewModelScope.launch {
            deviceLocation.collect { loc ->
                sharedLocation.value = loc?.let { Coordinates(it.latitude, it.longitude) }
            }
        }
    }

    fun updateDeviceLocation(location: Location?) {
        deviceLocation.value = location
        sharedLocation.value = location?.let { Coordinates(it.latitude, it.longitude) }
    }

    fun refresh() = logic.refresh()
    fun loadNextIfNeeded(index: Int) = logic.loadNextIfNeeded(index)
    fun onCenteredOrderChange(order: OrderInfo, index: Int= 0) =
        logic.onCenteredOrderChange(order, index)

    override fun onCleared() {
        logic.onClear()
        super.onCleared()
    }
}