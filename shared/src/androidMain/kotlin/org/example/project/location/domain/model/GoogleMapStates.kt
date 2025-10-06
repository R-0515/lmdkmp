package org.example.project.location.domain.model

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState
import org.example.project.map.domain.model.MapUiState

interface IMapStates {
    fun move(update: MapCameraUpdate)
    suspend fun animate(update: MapCameraUpdate)
    fun updateMarker(coords: Coordinates)
}

data class MapCameraUpdate(val raw: CameraUpdate)

fun cameraUpdateZoom(coords: Coordinates, zoom: Float): MapCameraUpdate {
    return MapCameraUpdate(
        CameraUpdateFactory.newLatLngZoom(
            coords.toLatLng(),
            zoom
        )
    )
}
class GoogleMapStates(
    val camera: CameraPositionState,
    val marker: MarkerState,
) : IMapStates {
    override fun move(update: MapCameraUpdate) = camera.move(update.raw)
    override suspend fun animate(update: MapCameraUpdate) = camera.animate(update.raw)
    override fun updateMarker(coords: Coordinates) {
        marker.position = coords.toLatLng()
    }
}

fun IMapStates.asGoogleMapsStates(): Pair<CameraPositionState, MarkerState> {
    val google = this as GoogleMapStates
    return google.camera to google.marker
}

data class MapConfig(
    val ui: MapUiState,
    val mapStates: IMapStates,
    val deviceCoords: Coordinates?,
    val canShowMyLocation: Boolean,
)
