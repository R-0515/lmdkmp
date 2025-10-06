package org.example.project.myPool.data.mapper

import org.example.project.myPool.data.remote.dto.OrderDto
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.domian.model.OrderStatus.Companion.fromId
import org.example.project.myPool.domian.model.RelativeTime
import org.example.project.util.AppDefaults


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
        status = OrderStatus.fromId(statusId) ?: OrderStatus.ADDED,
        price = price,
        customerPhone = phone,
        customerId = customerId,
        assignedAgentId = assignedAgentId,
        details = null,
    )
