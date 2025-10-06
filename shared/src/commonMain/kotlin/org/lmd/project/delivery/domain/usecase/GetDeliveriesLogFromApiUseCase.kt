package org.lmd.project.delivery.domain.usecase

import org.lmd.project.delivery.domain.model.DeliveryLog
import org.lmd.project.delivery.domain.model.Page
import org.lmd.project.delivery.domain.repository.DeliveriesLogRepository

object DeliveryStatusIds {
    const val CANCELLED = 3
    const val FAILED = 7
    const val DELIVERED = 8
    val DEFAULT_LOG_STATUSES: List<Int> = listOf(CANCELLED, FAILED, DELIVERED)
}

class GetDeliveriesLogPageUseCase(
    private val repo: DeliveriesLogRepository,
) {
    suspend operator fun invoke(
        page: Int,
        limit: Int,
        statusIds: List<Int> = DeliveryStatusIds.DEFAULT_LOG_STATUSES,
        search: String? = null,
    ): Result<Page<DeliveryLog>> = runCatching {
        repo.getLogsPage(page, limit, statusIds, search)
    }
}