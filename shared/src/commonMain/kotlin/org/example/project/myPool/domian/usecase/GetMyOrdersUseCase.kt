package org.example.project.myPool.domian.usecase

import org.example.project.myPool.domian.repository.MyOrdersRepository

class GetMyOrdersUseCase(
    private val repo: MyOrdersRepository,
) {
    suspend operator fun invoke(
        page: Int,
        limit: Int,
        bypassCache: Boolean = false,
        assignedAgentId: String? = null,
        userOrdersOnly: Boolean? = null,
    ): OrdersPage = repo.getOrders(page, limit, bypassCache, assignedAgentId, userOrdersOnly)
}