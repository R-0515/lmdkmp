package org.example.project.myPool.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.update
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.domian.util.OrdersPaging
import org.example.project.myPool.ui.logic.OrdersStatusLogic
import org.example.project.myPool.ui.logic.OrdersStore
import kotlin.text.get

class OrdersStatusViewModel(
    store: OrdersStore,
) : ViewModel() {

    private val logic = OrdersStatusLogic(store)

    fun updateStatusLocally(
        id: String,
        newStatus: OrderStatus,
        newAssigneeId: String? = null,
    ) = logic.updateStatusLocally(id, newStatus, newAssigneeId)

    fun applyServerPatch(updated: OrderInfo) = logic.applyServerPatch(updated)
}