package org.example.project.delivery.domain.model

import org.example.project.delivery.data.dto.OrderDto
import org.example.project.delivery.domain.usecase.DeliveryStatusIds

private fun mapState(statusId: Int?): DeliveryState =
    when (statusId) {
        DeliveryStatusIds.DELIVERED -> DeliveryState.DELIVERED
        DeliveryStatusIds.FAILED -> DeliveryState.FAILED
        DeliveryStatusIds.CANCELLED -> DeliveryState.CANCELLED
        else -> DeliveryState.OTHER
    }

fun OrderDto.toDomain(): DeliveryLog =
    DeliveryLog(
        number = "#${orderNumber}",
        createdAt = orderDate.orEmpty(),
        deliveryEtaText = deliveryTime,
        state = mapState(statusId),
    )