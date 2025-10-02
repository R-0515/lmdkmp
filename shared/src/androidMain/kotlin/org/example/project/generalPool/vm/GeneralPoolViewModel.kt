package org.example.project.generalPool.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase
import org.example.project.generalPool.domain.model.GeneralPoolUiState
import org.example.project.generalPool.domain.usecase.LoadOrdersUseCase
import org.example.project.generalPool.domain.usecase.OrdersRealtimeUseCase

class GeneralPoolViewModel(
    ordersRealtime: OrdersRealtimeUseCase,
    computeDistances: ComputeDistancesUseCase,
    getDeviceLocations: GetDeviceLocationsUseCase,
    loadOrdersUseCase: LoadOrdersUseCase,
) : ViewModel() {

    private val controller = GeneralPoolController(
        scope = viewModelScope,
        ordersRealtime = ordersRealtime,
        computeDistances = computeDistances,
        getDeviceLocations = getDeviceLocations,
        loadOrdersUseCase = loadOrdersUseCase
    )

    // Expose the same API as before
    val ui: StateFlow<GeneralPoolUiState> = controller.ui
    val events: SharedFlow<GeneralPoolUiEvent> = controller.events

    // Convert shared Coordinates → Android LatLng for map UI
    val deviceLatLng: StateFlow<LatLng?> =
        controller.deviceCoordinates
            .map { it?.toLatLng() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Forwarders
    val onSearchingChange get() = controller.onSearchingChange
    val onSearchTextChange get() = controller.onSearchTextChange
    val onOrderSelected get() = controller.onOrderSelected

    fun setCurrentUserId(id: String?) = controller.setCurrentUserId(id)
    fun attach() = controller.attach()

    override fun onCleared() {
        controller.clear()
        super.onCleared()
    }

    fun onDistanceChange(km: Double) = controller.onDistanceChange(km)

    fun handleLocationPermission(
        granted: Boolean,
        promptIfMissing: Boolean = false,
    ) = controller.handleLocationPermission(granted, promptIfMissing)
}

private fun Coordinates.toLatLng(): LatLng = LatLng(latitude, longitude)
