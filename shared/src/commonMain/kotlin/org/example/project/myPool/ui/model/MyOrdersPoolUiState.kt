package org.example.project.myPool.ui.model

import org.example.project.location.domain.model.Coordinates
import org.example.project.map.domain.model.MapMarker
import org.example.project.map.domain.model.MapUiState
import org.example.project.myPool.domian.model.OrderInfo

data class MyOrdersPoolUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val orders: List<OrderInfo> = emptyList(),
    val selectedOrderNumber: String? = null,
    override val distanceThresholdKm: Double = Double.MAX_VALUE,
    override val hasLocationPerm: Boolean = false,
) : MapUiState {
    companion object {
        private const val MAX_LATITUDE = 90.0
        private const val MAX_LONGITUDE = 180.0
    }

    override val markers: List<MapMarker>
        get() =
            orders
                .filter {
                    it.lat.isFinite() &&
                            it.lng.isFinite() &&
                            !(it.lat == 0.0 && it.lng == 0.0) &&
                            kotlin.math.abs(it.lat) <= MAX_LATITUDE &&
                            kotlin.math.abs(it.lng) <= MAX_LONGITUDE
                }.map {
                    MapMarker(
                        id = it.id,
                        title = it.name,
                        coordinates = Coordinates(it.lat, it.lng),
                        distanceKm = it.distanceKm,
                        snippet = it.orderNumber,
                    )
                }.let { base ->
                    if (!hasLocationPerm) return@let base
                    val anyFinite = base.any { it.distanceKm.isFinite() }
                    if (!anyFinite) return@let emptyList()
                    base.filter { it.distanceKm.isFinite() && it.distanceKm <= distanceThresholdKm }
                }

    override val selectedMarkerId: String?
        get() = selectedOrderNumber
}