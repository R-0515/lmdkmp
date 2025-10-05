package org.example.project.orderhistory.domain.repository

import org.example.project.orderhistory.data.repositoryimpl.OrdersRepositoryImpl
import org.example.project.orderhistory.domain.model.OrderHistoryUi


interface OrdersRepository {
    suspend fun getOrders(page: Int, limit: Int): OrdersRepositoryImpl.Paged<OrderHistoryUi>
}
