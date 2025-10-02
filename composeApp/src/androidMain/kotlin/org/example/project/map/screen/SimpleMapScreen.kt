package org.example.project.map.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.example.project.location.screen.permissions.locationPermissionHandler
import org.example.project.map.vm.MapLocationViewModel

@Composable
fun MapScreen(
    vm: MapLocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val location by vm.location.collectAsStateWithLifecycle() // lifecycle-aware
    val cameraState = rememberCameraPositionState()

    // Ask permission, then trigger loading the device location
    locationPermissionHandler(
        onPermissionGranted = { vm.loadMyLocation() },
        onPermissionDenied = { /* show a snackbar / state */ },
        requestOnLaunch = true
    )

    // When we receive a location, move/animate the camera to it
    LaunchedEffect(location) {
        location?.let {
            cameraState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude), 15f
                )
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraState,
        properties = MapProperties(
            isMyLocationEnabled = location != null
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = false
        )
    ) {
        location?.let {
            Marker(
                state = MarkerState(LatLng(it.latitude, it.longitude)),
                title = "current location"
            )
        }
    }
}
