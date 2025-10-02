package org.example.project.generalPool.domain.usecase

import kotlinx.coroutines.flow.StateFlow
import org.example.project.generalPool.domain.repository.LiveOrdersRepository
import org.example.project.socket.Order

class OrdersRealtimeUseCase(
    private val repo: LiveOrdersRepository,
) {
    fun connect(channelName: String = "orders") = repo.connectToOrders(channelName)

    fun disconnect() = repo.disconnectFromOrders()

    fun retry() = repo.retryConnection()

    fun orders(): StateFlow<List<Order>> = repo.orders()
}
