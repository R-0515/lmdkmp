package org.example.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import org.example.project.map.GeoPoint
import org.example.project.map.MapMarker
import org.example.project.map.MapProps

private fun GeoPoint.toLatLng() = LatLng(lat, lng)

@Composable
fun MapViewAndroid(
    props: MapProps,
    onMapClick: (GeoPoint) -> Unit = {},
    onMarkerClick: (MapMarker) -> Boolean = { false }
) {
    val initial = props.camera.center.toLatLng()
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initial, props.camera.zoom)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraState,
        properties = MapProperties(isMyLocationEnabled = props.myLocationEnabled),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = props.myLocationEnabled
        ),
        onMapClick = { latLng -> onMapClick(GeoPoint(latLng.latitude, latLng.longitude)) }
    ) {
        props.markers.forEach { m ->
            Marker(
                state = MarkerState(position = m.position.toLatLng()),
                title = m.title,
                snippet = m.snippet,
                zIndex = m.zIndex,
                onClick = { onMarkerClick(m) }
            )
        }
    }
}
