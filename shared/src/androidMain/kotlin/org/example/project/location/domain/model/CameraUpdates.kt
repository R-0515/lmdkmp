package org.example.project.location.domain.model

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory

data class MapCameraUpdate(val raw: CameraUpdate)

fun cameraUpdateZoom(coords: Coordinates, zoom: Float): MapCameraUpdate {
    return MapCameraUpdate(
        CameraUpdateFactory.newLatLngZoom(
            coords.toLatLng(),
            zoom
        )
    )
}