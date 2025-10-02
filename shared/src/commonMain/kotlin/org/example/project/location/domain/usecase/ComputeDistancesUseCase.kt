package org.example.project.location.domain.usecase

import org.example.project.location.domain.model.Coordinates
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

class ComputeDistancesUseCase {
    companion object {
        private const val EARTH_RADIUS_KM = 6371.0
        private const val MAX_LATITUDE = 90.0
        private const val MAX_LONGITUDE = 180.0
        private const val DEG_TO_RAD = PI / 180.0
    }

    private inline fun Double.rad(): Double = this * DEG_TO_RAD

    fun computeDistances(
        origin: Coordinates,
        targets: List<Coordinates>
    ): List<Double> = targets.map { target ->
        if (isValidLatLng(target.latitude, target.longitude)) {
            distanceKm(origin, target)
        } else {
            Double.POSITIVE_INFINITY
        }
    }

    fun isValidLatLng(lat: Double, lng: Double): Boolean {
        val finite = lat.isFinite() && lng.isFinite()
        val nonZero = !(lat == 0.0 && lng == 0.0)
        val inBounds = abs(lat) <= MAX_LATITUDE && abs(lng) <= MAX_LONGITUDE
        return finite && nonZero && inBounds
    }

    fun distanceKm(from: Coordinates, to: Coordinates): Double {
        val dLat = (to.latitude - from.latitude).rad()
        val dLng = (to.longitude - from.longitude).rad()
        val lat1 = from.latitude.rad()
        val lat2 = to.latitude.rad()

        // Haversine
        val a = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }
}
