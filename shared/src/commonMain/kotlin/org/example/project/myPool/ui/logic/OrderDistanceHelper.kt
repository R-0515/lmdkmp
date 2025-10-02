package org.example.project.myPool.ui.logic

import org.example.project.location.domain.model.Coordinates
import org.example.project.myPool.domian.model.OrderInfo


object OrderDistanceHelper {
    fun applyDistances(
        origin: Coordinates?,
        orders: List<OrderInfo>,
        compute: (Coordinates, List<Coordinates>) -> List<Double>,
    ): List<OrderInfo> {
        if (origin == null) return orders

        val coords = orders.map { Coordinates(it.lat, it.lng) }
        val distances = compute(origin, coords)
        return orders
            .zip(distances) { order, dist -> order.copy(distanceKm = dist) }
            .sortedBy { it.distanceKm }
    }
}