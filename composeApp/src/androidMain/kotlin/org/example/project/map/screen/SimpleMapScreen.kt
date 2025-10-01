package org.example.project.map.screen

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.project.location.screen.permissions.locationPermissionHandler
import org.example.project.map.components.MyLocationMap
import org.example.project.map.vm.MyLocationViewModel

@Composable
fun SimpleMapScreen(
    vm: MyLocationViewModel = viewModel()
) {
    // Ask for permission; when granted, tell the VM to fetch the location once
    locationPermissionHandler(
        onPermissionGranted = {
            vm.loadMyLocation()
        },
        onPermissionDenied = {
            // no-op; the map will stay at fallback without a marker
        },
        requestOnLaunch = true
    )

    val myLocation = vm.location.collectAsStateWithLifecycle().value
    MyLocationMap(location = myLocation)
}