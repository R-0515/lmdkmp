package org.lmd.project.orderhistory.domain.usecase

import org.lmd.project.orderhistory.domain.repository.OrdersRepository

class GetOrdersUseCase(private val repo: OrdersRepository) {
    suspend operator fun invoke(page: Int, limit: Int) = repo.getOrders(page, limit)
}
