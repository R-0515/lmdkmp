package org.lmd.project.generalPool.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.lmd.project.generalPool.domain.repository.LiveOrdersRepository
import org.lmd.project.socket.Order

class OrdersRealtimeUseCase(
    private val repo: LiveOrdersRepository,
) {
    suspend fun connect(channelName: String = "orders") = repo.connectToOrders(channelName)

    suspend fun disconnect() = repo.disconnectFromOrders()

    suspend fun retry() = repo.retryConnection()

    fun orders(): StateFlow<List<Order>> = repo.orders()
}
