package org.lmd.project.orderhistory.domain.repository

import org.lmd.project.orderhistory.data.repositoryimpl.OrdersRepositoryImpl
import org.lmd.project.orderhistory.domain.model.OrderHistoryUi


interface OrdersRepository {
    suspend fun getOrders(page: Int, limit: Int): OrdersRepositoryImpl.Paged<OrderHistoryUi>
}
