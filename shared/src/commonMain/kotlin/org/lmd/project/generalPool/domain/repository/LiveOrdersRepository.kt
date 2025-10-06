package org.lmd.project.generalPool.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.lmd.project.socket.Order

interface LiveOrdersRepository {
    suspend fun getAllLiveOrders(pageSize: Int): Result<List<Order>>

    suspend fun connectToOrders(channelName: String = "orders")

    suspend fun disconnectFromOrders()

    suspend fun retryConnection()

    fun updateOrderStatus(
        orderId: String,
        status: String,
    )

    fun orders(): StateFlow<List<Order>>

}
