package org.example.project.map.domain.model

import org.example.project.location.domain.model.Coordinates

data class MapMarker(
    val id: String,
    val position: Coordinates,
    val title: String? = null,
    val snippet: String? = null,
    val zIndex: Float = 0f
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
