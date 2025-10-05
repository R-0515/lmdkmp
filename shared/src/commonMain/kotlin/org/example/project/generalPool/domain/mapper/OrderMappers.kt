package org.example.project.generalPool.domain.mapper

import org.example.project.generalPool.domain.model.OrderDto
import org.example.project.generalPool.domain.model.OrderInfo
import org.example.project.generalPool.domain.model.OrderStatus.Companion.fromId
import org.example.project.generalPool.domain.model.RelativeTime

private const val METERS_PER_KILOMETER = 1_000.0

private fun Double?.toKmOrDefault(default: Double): Double {
    val meters = this ?: return default
    return if (meters.isFinite() && meters >= 0.0) meters / METERS_PER_KILOMETER else default
}

fun OrderDto.toDomain(): OrderInfo =
    OrderInfo(
        id = orderId,
        name = customerName.orEmpty(),
        orderNumber = orderNumber,
        timeAgo = RelativeTime.JustNow,
        itemsCount = 0,
        distanceKm = distanceKm.toKmOrDefault(AppDefaults.DEFAULT_DISTANCE_KM),
        lat = coordinates?.latitude ?: AppDefaults.DEFAULT_LAT,
        lng = coordinates?.longitude ?: AppDefaults.DEFAULT_LNG,
        status = fromId(statusId),
        price = price,
        customerPhone = phone,
        customerId = customerId,
        assignedAgentId = assignedAgentId,
        details = null,
    )

object AppDefaults {
    const val DEFAULT_LAT = 24.7136 // Riyadh, KSA
    const val DEFAULT_LNG = 46.6753 // Riyadh, KSA
    const val DEFAULT_DISTANCE_KM = 0.0 // Safe value for calculations
}
