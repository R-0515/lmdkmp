package org.lmd.project.generalPool.domain.usecase

import org.lmd.project.generalPool.domain.repository.LiveOrdersRepository
import org.lmd.project.socket.Order

class LoadOrdersUseCase(
    private val repo: LiveOrdersRepository,
) {
    suspend operator fun invoke(pageSize: Int): Result<List<Order>> =
        repo.getAllLiveOrders(pageSize)
}
