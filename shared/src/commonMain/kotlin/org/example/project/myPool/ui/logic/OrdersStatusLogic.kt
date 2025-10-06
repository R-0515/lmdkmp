package org.example.project.myPool.ui.logic

import kotlinx.coroutines.flow.update
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.domian.util.OrdersPaging

class OrdersStatusLogic(
    private val store: OrdersStore,
) {
    fun updateStatusLocally(
        id: String,
        newStatus: OrderStatus,
        newAssigneeId: String? = null,
    ) {
        // Update visible list
        val updatedVisible = store.state.value.orders.map { o ->
            if (o.id == id) {
                o.copy(
                    status = newStatus,
                    assignedAgentId = newAssigneeId ?: o.assignedAgentId,
                )
            } else o
        }
        store.state.update { it.copy(orders = updatedVisible) }

        // Update in memory list
        val index = store.allOrders.indexOfFirst { it.id == id }
        if (index != -1) {
            store.allOrders[index] = store.allOrders[index].copy(
                status = newStatus,
                assignedAgentId = newAssigneeId ?: store.allOrders[index].assignedAgentId,
            )
        }

        reapplyFilter()
    }

    fun applyServerPatch(updated: OrderInfo) {
        val visible = store.state.value.orders.toMutableList()
        val i = visible.indexOfFirst { it.id == updated.id }
        if (i != -1) {
            visible[i] = visible[i].copy(
                status = updated.status,
                details = updated.details ?: visible[i].details,
                assignedAgentId = updated.assignedAgentId,
            )
            store.state.update { it.copy(orders = visible) }
        }

        val j = store.allOrders.indexOfFirst { it.id == updated.id }
        if (j != -1) {
            store.allOrders[j] = store.allOrders[j].copy(
                status = updated.status,
                details = updated.details ?: store.allOrders[j].details,
                assignedAgentId = updated.assignedAgentId,
            )
        }

        reapplyFilter()
    }

    private fun reapplyFilter() {
        val uid = store.currentUserId.value
        val query = store.state.value.query.trim()

        val filtered = store.allOrders.filter { o ->
            val matchesQuery = query.isBlank() ||
                    o.orderNumber.contains(query, ignoreCase = true) ||
                    o.name.contains(query, ignoreCase = true) ||
                    (o.details?.contains(query, ignoreCase = true) == true)

            val matchesUser = uid.isNullOrBlank() || o.assignedAgentId == uid
            matchesQuery && matchesUser
        }

        val pageSize = OrdersPaging.PAGE_SIZE
        store.state.update { it.copy(orders = filtered.take(pageSize)) }
    }
}
