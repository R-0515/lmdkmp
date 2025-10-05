package org.example.project.generalPool.domain.usecase

import org.example.project.generalPool.domain.model.OrderInfo
import org.example.project.generalPool.domain.repository.UpdateOrdersStatusRepository

class UpdateOrderStatusUseCase(
    private val repo: UpdateOrdersStatusRepository,
) {
    suspend operator fun invoke(
        orderId: String,
        statusId: Int,
        assignedAgentId: String? = null,
    ): OrderInfo = repo.updateOrderStatus(orderId, statusId, assignedAgentId)
}
