package org.example.project.myPool.ui.viewmodel.myOrder

import android.util.Log
import androidx.lifecycle.ViewModel
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.ui.logic.myOrderLogic.OrdersStatusLogic
import org.example.project.myPool.ui.state.OrdersStore

class OrdersStatusViewModel(
    store: OrdersStore
) : ViewModel() {

    private val logic = OrdersStatusLogic(
        store,
        logger = { msg -> Log.d("ReassignFlow", msg) }
    )

    fun updateStatusLocally(id: String, newStatus: OrderStatus, newAssigneeId: String? = null) =
        logic.updateStatusLocally(id, newStatus, newAssigneeId)

    fun applyServerPatch(updated: OrderInfo) =
        logic.applyServerPatch(updated)
}