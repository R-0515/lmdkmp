package org.example.project.myPool.domian.usecase

import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.repository.UpdateOrdersStatusRepository

class UpdateOrderStatusUseCase(
    private val repo: UpdateOrdersStatusRepository,
) {
    suspend operator fun invoke(
        orderId: String,
        statusId: Int,
        assignedAgentId: String? = null,
    ): OrderInfo = repo.updateOrderStatus(orderId, statusId, assignedAgentId)
}