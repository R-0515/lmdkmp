package org.example.project.myPool.ui.logic

import android.util.Log
import org.example.project.myPool.domian.model.OrderStatus

actual class OrderLogger {
    private val TAG = "OrderAction"

    actual fun uiTap(orderId: String, orderNumber: String?, action: String) {
        Log.d(TAG, "UI → tap action=$action | orderId=$orderId orderNo=${orderNumber ?: "-"}")
    }

    actual fun postStart(orderId: String, target: OrderStatus) {
        Log.i(TAG, "API → POST /update-order-status start | orderId=$orderId target=$target")
    }

    actual fun postSuccess(orderId: String, new: OrderStatus?) {
        Log.i(TAG, "API ← success | orderId=$orderId newStatus=$new")
    }

    actual fun postError(orderId: String, target: OrderStatus, t: Throwable) {
        Log.e(TAG, "API ← error | orderId=$orderId target=$target msg=${t.message}", t)
    }
}