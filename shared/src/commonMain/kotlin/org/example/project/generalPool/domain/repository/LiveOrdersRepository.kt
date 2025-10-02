package org.example.project.generalPool.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.example.project.socket.Order

interface LiveOrdersRepository {
    suspend fun getAllLiveOrders(pageSize: Int): Result<List<Order>>

    fun connectToOrders(channelName: String = "orders")

    fun disconnectFromOrders()

    fun retryConnection()

    fun updateOrderStatus(
        orderId: String,
        status: String,
    )

    fun orders(): StateFlow<List<Order>>

}
