package org.example.project.myPool.domian.repository

import org.example.project.myPool.domian.model.OrderInfo

interface UpdateOrdersStatusRepository {
    suspend fun updateOrderStatus(
        orderId: String,
        statusId: Int,
        assignedAgentId: String?,
    ): OrderInfo
}