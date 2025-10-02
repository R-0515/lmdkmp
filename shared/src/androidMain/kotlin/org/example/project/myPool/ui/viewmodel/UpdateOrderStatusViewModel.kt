package org.example.project.myPool.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.domian.usecase.UpdateOrderStatusUseCase
import org.example.project.myPool.ui.logic.UpdateOrderStatusLogic

class UpdateOrderStatusViewModel(
    updateStatus: UpdateOrderStatusUseCase,
    userStore: SecureUserStore,
) : ViewModel() {

    val logic = UpdateOrderStatusLogic(updateStatus, userStore, viewModelScope)

    object OrderLogger {
        private const val TAG = "OrderAction"

        fun uiTap(orderId: String, orderNumber: String?, action: String) {
            Log.d(TAG, "UI → tap action=$action | orderId=$orderId orderNo=${orderNumber ?: "-"}")
        }

        fun postStart(orderId: String, target: OrderStatus) {
            Log.i(TAG, "API → POST /update-order-status start | orderId=$orderId target=$target")
        }

        fun postSuccess(orderId: String, new: OrderStatus?) {
            Log.i(TAG, "API ← success | orderId=$orderId newStatus=$new")
        }

        fun postError(orderId: String, target: OrderStatus, t: Throwable) {
            Log.e(TAG, "API ← error | orderId=$orderId target=$target msg=${t.message}", t)
        }
    }
}