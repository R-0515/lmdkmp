package org.example.project

import androidx.compose.runtime.Composable
import org.example.project.map.GeoPoint
import org.example.project.map.MapCamera
import org.example.project.map.MapMarker
import org.example.project.map.MapProps

@Composable
fun SimpleMapScreen(
    props: MapProps = MapProps(
        camera = MapCamera(
            center = GeoPoint(24.7136, 46.6753),
            zoom = 12f
        ),
        markers = listOf(
            MapMarker(
                id = "riyadh",
                position = GeoPoint(24.7136, 46.6753),
                title = "Riyadh",
                snippet = "Hello, map!"
            )
        ),
        myLocationEnabled = false
    )
) {
    MapViewAndroid(props)
}