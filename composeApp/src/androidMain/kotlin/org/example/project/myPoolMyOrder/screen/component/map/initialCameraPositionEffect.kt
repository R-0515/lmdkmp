package org.example.project.myPoolMyOrder.screen.component.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.example.project.location.domain.model.IMapStates
import org.example.project.location.domain.model.cameraUpdateZoom
import org.example.project.location.domain.model.isValid
import org.example.project.map.domain.model.MapMarker

private const val INITIAL_CAMERA_ZOOM = 14f
private const val MY_ORDER_FOCUS_ZOOM = 15f

@Composable
fun initialCameraPositionEffect(
    markers: List<MapMarker>,
    selectedMarkerId: String?,
    mapStates: IMapStates,
) {
    var didInitialCamera by remember { mutableStateOf(false) }

    LaunchedEffect(markers, selectedMarkerId) {
        if (!didInitialCamera && markers.isNotEmpty() && selectedMarkerId == null) {
            val first = markers.firstOrNull { it.coordinates.isValid() } ?: return@LaunchedEffect
            didInitialCamera = true
            mapStates.updateMarker(first.coordinates)
            mapStates.move(
                cameraUpdateZoom(
                    first.coordinates,
                    INITIAL_CAMERA_ZOOM
                )
            )
        }
    }
}

@Composable
fun rememberFocusOnMarker(
    onCenterChange: (MapMarker) -> Unit,
    mapStates: IMapStates,
    scope: CoroutineScope,
    focusZoom: Float = MY_ORDER_FOCUS_ZOOM,
): (MapMarker) -> Unit {
    val updatedCenter = rememberUpdatedState(onCenterChange)
    val updatedStates = rememberUpdatedState(mapStates)
    val updatedScope = rememberUpdatedState(scope)

    return remember {
        { marker: MapMarker ->
            updatedCenter.value(marker)
            if (marker.coordinates.isValid()) {
                updatedStates.value.updateMarker(marker.coordinates)
                updatedScope.value.launch {
                    updatedStates.value.animate(
                        cameraUpdateZoom(
                            marker.coordinates,
                            focusZoom
                        )
                    )
                }
            }
        }
    }
}