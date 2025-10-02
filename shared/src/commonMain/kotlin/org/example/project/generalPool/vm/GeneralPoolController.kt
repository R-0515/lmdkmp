package org.example.project.generalPool.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase
import org.example.project.generalPool.domain.mapper.toUi
import org.example.project.generalPool.domain.model.GeneralPoolUiState
import org.example.project.generalPool.domain.model.Order
import org.example.project.generalPool.domain.model.OrderInfo
import org.example.project.generalPool.domain.usecase.LoadOrdersUseCase
import org.example.project.generalPool.domain.usecase.OrdersRealtimeUseCase

sealed class GeneralPoolUiEvent {
    data object RequestLocationPermission : GeneralPoolUiEvent()
}

class GeneralPoolController(
    private val scope: CoroutineScope,
    private val ordersRealtime: OrdersRealtimeUseCase,
    private val computeDistances: ComputeDistancesUseCase,
    private val getDeviceLocations: GetDeviceLocationsUseCase,
    private val loadOrdersUseCase: LoadOrdersUseCase,
) {
    private val _ui = MutableStateFlow(GeneralPoolUiState())
    val ui: StateFlow<GeneralPoolUiState> = _ui.asStateFlow()

    private val _events = MutableSharedFlow<GeneralPoolUiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<GeneralPoolUiEvent> = _events.asSharedFlow()

    private val _deviceCoordinates = MutableStateFlow<Coordinates?>(null)
    val deviceCoordinates: StateFlow<Coordinates?> = _deviceCoordinates.asStateFlow()

    private var realtimeStarted = false
    private var realtimeJob: Job? = null

    private var lastNonEmptyOrders: List<OrderInfo> = emptyList()
    private var userPinnedSelection: Boolean = false
    private var currentUserId: String? = null

    // UI callbacks
    val onSearchingChange: (Boolean) -> Unit = { v ->
        _ui.update { it.copy(searching = v) }
    }
    val onSearchTextChange: (String) -> Unit = { v ->
        _ui.update { it.copy(searchText = v) }
    }
    val onOrderSelected: (OrderInfo?) -> Unit = { order ->
        userPinnedSelection = order != null
        _ui.update { it.copy(selected = order) }
    }

    fun setCurrentUserId(id: String?) {
        currentUserId = id?.trim()?.ifEmpty { null }
    }

    fun attach() {
        if (!realtimeStarted) startRealtime()
        loadOrdersFromApi()
    }

    fun clear() {
//        ordersRealtime.disconnect()
        realtimeJob?.cancel()
        realtimeStarted = false
    }

    private fun startRealtime() {
        realtimeStarted = true
//        ordersRealtime.connect("orders")
        realtimeJob?.cancel()
//        realtimeJob = scope.launch {
//            ordersRealtime.orders().collect { handleLiveOrders(it) }
//        }
    }

    private suspend fun handleLiveOrders(liveOrders: List<Order>) {
        val incoming = liveOrders.map { it.toUi() }.poolVisible(currentUserId)
        val merged = mergeOrders(_ui.value.orders, incoming).poolVisible(currentUserId)
        val nextSel = determineNextSelection(merged, _ui.value.selected, userPinnedSelection)

        _ui.update { it.copy(orders = merged, selected = nextSel ?: it.selected) }
        if (merged.isNotEmpty()) lastNonEmptyOrders = merged

        if (_ui.value.hasLocationPerm) {
            scope.launch {
                val (last, current) = getDeviceLocations()
                val origin = current ?: last ?: return@launch
                applyDistances(origin)
            }
        }
        _ui.ensureSelectedStillVisible { removeInvalidSelectionIfNeeded() }
    }

    fun onDistanceChange(km: Double) {
        _ui.update { it.copy(distanceThresholdKm = km) }
        _ui.ensureSelectedStillVisible { removeInvalidSelectionIfNeeded() }
    }

    fun handleLocationPermission(
        granted: Boolean,
        promptIfMissing: Boolean = false,
    ) {
        _ui.update { it.copy(hasLocationPerm = granted) }
        if (granted) {
            scope.launch {
                val (last, current) = getDeviceLocations()
                val origin = current ?: last ?: return@launch
                applyDistances(origin)
            }
        } else if (promptIfMissing) {
            _events.tryEmit(GeneralPoolUiEvent.RequestLocationPermission)
        }
    }

    private fun applyDistances(origin: Coordinates) {
        val orders = _ui.value.orders
        val targets = orders.map { Coordinates(it.lat, it.lng) }
        val distances = computeDistances.computeDistances(origin, targets)

        val updated = orders.indices.map { i ->
            orders[i].copy(distanceKm = distances.getOrNull(i) ?: Double.POSITIVE_INFINITY)
        }

        val nextSelected =
            determineSelectionAfterDistanceUpdate(_ui.value.selected, updated, userPinnedSelection)

        _ui.update(updateUiWithDistances(updated, nextSelected) { lastNonEmptyOrders = it })
        _deviceCoordinates.value = origin
        _ui.ensureSelectedStillVisible { this }
    }

    private fun loadOrdersFromApi() {
        _ui.update { it.copy(isLoading = true) }
        scope.launch {
            val result = loadOrdersUseCase(pageSize = 25)
            result
                .onSuccess { allOrders ->
                    val initial = allOrders.map { it.toUi() }.poolVisible(currentUserId)
                    val defaultSel = pickDefaultSelection(_ui.value.selected, initial)
                    userPinnedSelection = false
                    _ui.update {
                        it.copy(
                            orders = initial,
                            isLoading = false,
                            selected = defaultSel,
                            errorMessage = null,
                        )
                    }
                    if (initial.isNotEmpty()) lastNonEmptyOrders = initial

                    if (_ui.value.hasLocationPerm) {
                        val (last, current) = getDeviceLocations()
                        val origin = current ?: last
                        if (origin != null) applyDistances(origin)
                    }
                    _ui.ensureSelectedStillVisible { removeInvalidSelectionIfNeeded() }
                }
                .onFailure { e ->
                    // KMP-safe log
                    println("GeneralPool: Failed to load orders: ${e.message}")
                    _ui.update { s ->
                        s.copy(isLoading = false, errorMessage = "Unable to load orders.")
                    }
                }
        }
    }
}