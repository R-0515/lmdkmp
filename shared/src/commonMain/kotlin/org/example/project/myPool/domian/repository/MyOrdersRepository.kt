package org.example.project.myPool.domian.repository

import org.example.project.myPool.domian.model.OrdersPage

interface MyOrdersRepository {
    suspend fun getOrders(
        page: Int,
        limit: Int,
        bypassCache: Boolean,
        assignedAgentId: String? = null,
        userOrdersOnly: Boolean? = null,
    ): OrdersPage
}