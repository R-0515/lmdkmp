package org.example.project.map.domain.model

import org.example.project.location.domain.model.Coordinates
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.util.AppDefaults

/*
data class MapMarker(
    val id: String,
    val position: Coordinates,
    val title: String? = null,
    val snippet: String? = null,
    val zIndex: Float = 0f
)
*/

data class MapMarker(
    val id: String,
    val title: String,
    val coordinates: Coordinates,
    val distanceKm: Double = 0.0,
    val snippet: String? = null,
)

data class MapCamera(
    val center: Coordinates,
    val zoom: Float
)

data class MapProps(
    val camera: MapCamera,
    val markers: List<MapMarker>,
    val myLocationEnabled: Boolean = false
)

fun OrderInfo.toMapMarker(): MapMarker =
    MapMarker(
        id = orderNumber,
        title = name,
        coordinates = Coordinates(lat, lng),
        snippet = orderNumber
    )

interface MapUiState {
    val markers: List<MapMarker>
    val selectedMarkerId: String?
    val distanceThresholdKm: Double
    val hasLocationPerm: Boolean
}

