package org.example.project.map.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.example.project.location.domain.model.Coordinates

private val DEFAULT_FALLBACK = Coordinates(24.4672, 39.6111)
private const val DEFAULT_ZOOM = 16f
private fun Coordinates.toLatLng() = LatLng(latitude, longitude)

@Composable
fun MyLocationMap(
    location: Coordinates?,
    modifier: Modifier = Modifier,
    showMyLocationDot: Boolean = true,
) {
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_FALLBACK.toLatLng(), DEFAULT_ZOOM)
    }

    // Animate camera when location changes
    LaunchedEffect(location) {
        location?.let {
            cameraState.animate(
                CameraUpdateFactory.newLatLngZoom(it.toLatLng(), DEFAULT_ZOOM)
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraState,
        properties = MapProperties(
            isMyLocationEnabled = showMyLocationDot && location != null
        ),
    ) {
        location?.let {
            Marker(
                state = MarkerState(it.toLatLng()),
                title = "Me"
            )
        }
    }
}
