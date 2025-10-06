package org.lmd.project.generalPool.domain.repository

import org.lmd.project.generalPool.domain.model.OrderInfo

interface UpdateOrdersStatusRepository {
    suspend fun updateOrderStatus(
        orderId: String,
        statusId: Int,
        assignedAgentId: String?,
    ): OrderInfo
}