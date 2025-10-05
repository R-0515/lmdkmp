package org.example.project.orderhistory.domain.usecase

import org.example.project.orderhistory.domain.repository.OrdersRepository

class GetOrdersUseCase(private val repo: OrdersRepository) {
    suspend operator fun invoke(page: Int, limit: Int) = repo.getOrders(page, limit)
}
