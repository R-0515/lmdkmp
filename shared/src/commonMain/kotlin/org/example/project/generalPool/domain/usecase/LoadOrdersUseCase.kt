package org.example.project.generalPool.domain.usecase

import org.example.project.generalPool.domain.repository.LiveOrdersRepository
import org.example.project.socket.Order

class LoadOrdersUseCase(
    private val repo: LiveOrdersRepository,
) {
    suspend operator fun invoke(pageSize: Int): Result<List<Order>> =
        repo.getAllLiveOrders(pageSize)
}
