package org.example.project.myPool.domian.repository

interface MyOrdersRepository {
    suspend fun getOrders(
        page: Int,
        limit: Int,
        bypassCache: Boolean,
        assignedAgentId: String? = null,
        userOrdersOnly: Boolean? = null,
    ): OrdersPage
}