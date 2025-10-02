package org.example.project.location.domain.model

import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState
import org.example.project.map.domain.model.IMapStates

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
