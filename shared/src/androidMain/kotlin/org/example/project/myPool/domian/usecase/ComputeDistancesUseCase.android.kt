package org.example.project.myPool.domian.usecase

import android.location.Location
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.socket.Coordinates
import kotlin.math.abs

/*
actual class ComputeDistancesUseCase actual constructor() {
    companion object {
        // meters -> kilometers
        private const val METERS_IN_KM = 1000.0

        // Geographic coordinate bounds
        private const val MAX_LATITUDE = 90.0
        private const val MAX_LONGITUDE = 180.0
    }

    // coordinates validation
    private fun isValidLatLng(
        lat: Double,
        lng: Double,
    ): Boolean {
        val finite = lat.isFinite() && lng.isFinite()
        val nonZeroPair = !(lat == 0.0 && lng == 0.0)
        val withinBounds = abs(lat) <= MAX_LATITUDE && abs(lng) <= MAX_LONGITUDE
        return finite && nonZeroPair && withinBounds
    }

    // for calculating distance between two coordinates (in km)
    private fun distanceKm(
        lat1: Double?,
        lng1: Double?,
        lat2: Double,
        lng2: Double,
    ): Double =
        try {
            val result = FloatArray(1)
            Location.distanceBetween(
                lat1?.toDouble() ?: 0.0,
                lng1?.toDouble() ?: 0.0,
                lat2,
                lng2,
                result
            )
            val meters = result.getOrNull(0)?.toDouble() ?: Double.POSITIVE_INFINITY
            if (meters.isFinite() && meters >= 0.0) meters / METERS_IN_KM else Double.POSITIVE_INFINITY
        } catch (_: Exception) {
            Double.POSITIVE_INFINITY
        }

    actual operator fun invoke(
        origin: Coordinates,
        orders: List<OrderInfo>
    ): List<OrderInfo> {
        if (orders.isEmpty()) return orders

        return orders
            .map { o ->
                val distKm =
                    if (isValidLatLng(o.lat, o.lng)) {
                        distanceKm(origin.latitude, origin.longitude, o.lat, o.lng)
                    } else {
                        // Unknown/invalid coordinates are pushed to the end
                        Double.POSITIVE_INFINITY
                    }
                // return a copy with computed distance
                o.copy(distanceKm = distKm)
            }
            // nearest first
            .sortedBy { it.distanceKm }
    }
}
*/
actual class ComputeDistancesUseCase actual constructor() {

    companion object {
        private const val METERS_IN_KM = 1000.0
        private const val MAX_LATITUDE = 90.0
        private const val MAX_LONGITUDE = 180.0
    }

    private fun isValidLatLng(lat: Double, lng: Double): Boolean {
        val finite = lat.isFinite() && lng.isFinite()
        val nonZeroPair = !(lat == 0.0 && lng == 0.0)
        val withinBounds = abs(lat) <= MAX_LATITUDE && abs(lng) <= MAX_LONGITUDE
        return finite && nonZeroPair && withinBounds
    }

    private fun distanceKm(
        lat1: Double?,
        lng1: Double?,
        lat2: Double,
        lng2: Double,
    ): Double = try {
        val result = FloatArray(1)
        Location.distanceBetween(lat1 ?: 0.0, lng1 ?: 0.0, lat2, lng2, result)
        val meters = result.getOrNull(0)?.toDouble() ?: Double.POSITIVE_INFINITY
        if (meters.isFinite() && meters >= 0.0) meters / METERS_IN_KM else Double.POSITIVE_INFINITY
    } catch (_: Exception) {
        Double.POSITIVE_INFINITY
    }

    actual operator fun invoke(
        origin: Coordinates,
        orders: List<OrderInfo>,
    ): List<OrderInfo> {
        if (orders.isEmpty()) return orders
        return orders.map { o ->
            val distKm = if (isValidLatLng(o.lat, o.lng)) {
                distanceKm(origin.lat, origin.lng, o.lat, o.lng)
            } else {
                Double.POSITIVE_INFINITY
            }
            o.copy(distanceKm = distKm)
        }.sortedBy { it.distanceKm }
    }
}