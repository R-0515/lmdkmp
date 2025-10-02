package org.example.project.map.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.location.screen.permissions.locationPermissionHandler
import org.example.project.map.components.MyLocationMap
import org.example.project.map.vm.MapLocationViewModel

@Composable
fun MapScreen(
    vm: MapLocationViewModel = viewModel()
) {
    val location by vm.location.collectAsStateWithLifecycle()

    // Ask permission, then trigger loading
    locationPermissionHandler(
        onPermissionGranted = { vm.loadMyLocation() },
        onPermissionDenied = { /* TODO show a message */ },
        requestOnLaunch = true
    )

    // Delegate rendering + camera behavior to the component
    MyLocationMap(
        location = location,
        showMyLocationDot = true,
        showMyLocationButton = true
    )
}
