package org.example.project.myPool.ui.state

import org.example.project.myPool.domian.model.OrderStatus

val AutoHideOnSuccessStatuses = setOf(
    OrderStatus.DELIVERY_DONE,
    OrderStatus.DELIVERY_FAILED,
    OrderStatus.REASSIGNED,
)