package org.lmd.project.delivery.data.repositoryimpl

import org.lmd.project.delivery.data.api.DeliveriesLogApi
import org.lmd.project.delivery.data.dto.toDeliveryLog
import org.lmd.project.delivery.domain.model.DeliveryLog
import org.lmd.project.delivery.domain.model.Page
import org.lmd.project.delivery.domain.repository.DeliveriesLogRepository


data class Page<T>(val items: List<T>, val hasNext: Boolean)

class DeliveriesLogRepositoryImpl(
    private val api: DeliveriesLogApi,
) : DeliveriesLogRepository {

    override suspend fun getLogsPage(
        page: Int,
        limit: Int,
        statusIds: List<Int>,
        search: String?,
    ): Page<DeliveryLog> {

        val statusFilter = statusIds.takeIf { it.isNotEmpty() }?.joinToString(",")

        val env = api.getOrders(
            page = page,
            limit = limit,
            statusIds = statusFilter,
            search = search,
            assignedAgentId = null,
            userOrdersOnly = false,
        )

        if (!env.success) error(env.error ?: "Unknown error from orders-list")

        val data = env.data ?: return Page(emptyList(), hasNext = false)

        val items = data.orders.map { it.toDeliveryLog() }
        val hasNext = data.pagination?.hasNextPage
            ?: (data.pagination?.totalPages?.let { page < it } ?: (items.size == limit))

        println("📦 Deliveries page=$page size=${items.size} hasNext=$hasNext")
        return Page(items, hasNext)
    }
}
