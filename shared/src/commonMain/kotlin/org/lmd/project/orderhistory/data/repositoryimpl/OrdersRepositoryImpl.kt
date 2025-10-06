package org.lmd.project.orderhistory.data.repositoryimpl

import org.lmd.project.orderhistory.data.api.OrdersHistoryApi
import org.lmd.project.orderhistory.domain.model.OrderHistoryUi
import org.lmd.project.orderhistory.domain.model.toUi
import org.lmd.project.orderhistory.domain.repository.OrdersRepository


class OrdersRepositoryImpl(
    private val api: OrdersHistoryApi,
) : OrdersRepository {

    data class Paged<T>(val items: List<T>, val hasNext: Boolean)

    override suspend fun getOrders(page: Int, limit: Int): Paged<OrderHistoryUi> = try {
        val res = api.getOrders(page = page, limit = limit)

        if (res.success && res.data != null) {
            val items = res.data.orders.map { it.toUi() }
            val hasNext = res.data.pagination?.hasNextPage
                ?: (items.size == limit)
            println("page=$page size=${items.size} hasNext=$hasNext")
            Paged(items, hasNext)
        } else {
            println("page=$page end: ${res.error}")
            Paged(emptyList(), hasNext = false)
        }
    } catch (e: Exception) {
        val msg = e.message.orEmpty()
        val isTail = msg.contains("Failed to fetch orders", ignoreCase = true)
        println("OrdersRepositoryImpl page=$page limit=$limit error: $msg (tail=$isTail)")
        Paged(emptyList(), hasNext = !isTail)
    }

}

