package org.example.project.myPool.ui.logic

import org.example.project.myPool.domian.model.OrderStatus

actual class OrderLogger actual constructor() {
    actual fun uiTap(orderId: String, orderNumber: String?, action: String) {
    }

    actual fun postStart(
        orderId: String,
        target: OrderStatus
    ) {
    }

    actual fun postSuccess(
        orderId: String,
        new: OrderStatus?
    ) {
    }

    actual fun postError(
        orderId: String,
        target: OrderStatus,
        t: Throwable
    ) {
    }
}