package org.example.project.myPoolMyOrder.screen.component.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.example.project.location.domain.model.GoogleMapStates
import org.example.project.map.domain.model.IMapStates


@Composable
fun provideMapStates(): IMapStates {
    val camera = rememberCameraPositionState()
    val marker = remember { MarkerState(LatLng(0.0, 0.0)) }
    return GoogleMapStates(camera, marker)
}