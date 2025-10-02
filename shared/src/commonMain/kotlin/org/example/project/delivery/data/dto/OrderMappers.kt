package org.example.project.delivery.data.dto

import org.example.project.delivery.domain.model.DeliveryLog
import org.example.project.delivery.domain.model.DeliveryState
import org.example.project.delivery.domain.usecase.DeliveryStatusIds

private fun mapState(statusId: Int?): DeliveryState =
    when (statusId) {
        DeliveryStatusIds.DELIVERED -> DeliveryState.DELIVERED
        DeliveryStatusIds.FAILED    -> DeliveryState.FAILED
        DeliveryStatusIds.CANCELLED -> DeliveryState.CANCELLED
        else                        -> DeliveryState.OTHER
    }

fun OrderDto.toDeliveryLog(): DeliveryLog =
    DeliveryLog(
        number = "#${orderNumber}",
        createdAt = orderDate.orEmpty(),
        deliveryEtaText = deliveryTime.orEmpty(),
        state = mapState(statusId),
    )
