package org.example.project.map

data class GeoPoint(
    val lat: Double,
    val lng: Double,
) {
    fun isValid(): Boolean =
        lat.isFinite() && lng.isFinite() &&
                !(lat == 0.0 && lng == 0.0) &&
                kotlin.math.abs(lat) <= 90.0 && kotlin.math.abs(lng) <= 180.0
}

data class MapMarker(
    val id: String,
    val position: GeoPoint,
    val title: String? = null,
    val snippet: String? = null,
    val zIndex: Float = 0f
)

data class MapCamera(
    val center: GeoPoint,
    val zoom: Float
)

data class MapProps(
    val camera: MapCamera,
    val markers: List<MapMarker>,
    val myLocationEnabled: Boolean = false
)

// Minimal bounds model that works on any platform
data class GeoBounds(
    val southWest: GeoPoint,
    val northEast: GeoPoint
) {
    fun isDegenerate(): Boolean =
        southWest.lat == northEast.lat && southWest.lng == northEast.lng
}

// What the UI should do with the camera
sealed class CameraPlan {
    data class FitBounds(val bounds: GeoBounds, val paddingPx: Int) : CameraPlan()
    data class CenterZoom(val center: GeoPoint, val zoom: Float) : CameraPlan()
}

fun computeGeoBounds(points: List<GeoPoint>): GeoBounds? {
    val filtered = points.filter { it.lat.isFinite() && it.lng.isFinite() }
    if (filtered.isEmpty()) return null

    var minLat = Double.POSITIVE_INFINITY
    var maxLat = Double.NEGATIVE_INFINITY
    var minLng = Double.POSITIVE_INFINITY
    var maxLng = Double.NEGATIVE_INFINITY

    for (p in filtered) {
        if (p.lat < minLat) minLat = p.lat
        if (p.lat > maxLat) maxLat = p.lat
        if (p.lng < minLng) minLng = p.lng
        if (p.lng > maxLng) maxLng = p.lng
    }
    return GeoBounds(
        southWest = GeoPoint(minLat, minLng),
        northEast = GeoPoint(maxLat, maxLng)
    )
}

fun planCameraForMarkers(
    markers: List<MapMarker>,
    fallbackCenter: GeoPoint,
    fallbackZoom: Float,
    paddingPx: Int = 100
): CameraPlan {
    val points = markers.map { it.position }
    val bounds =
        computeGeoBounds(points) ?: return CameraPlan.CenterZoom(fallbackCenter, fallbackZoom)
    return if (bounds.isDegenerate()) {
        // Single point
        CameraPlan.CenterZoom(bounds.southWest, fallbackZoom)
    } else {
        CameraPlan.FitBounds(bounds, paddingPx)
    }
}
