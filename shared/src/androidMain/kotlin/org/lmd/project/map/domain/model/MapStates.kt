package org.lmd.project.map.domain.model

import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerState

data class MapStates(
    val cameraPositionState: CameraPositionState,
    val markerState: MarkerState,
)
