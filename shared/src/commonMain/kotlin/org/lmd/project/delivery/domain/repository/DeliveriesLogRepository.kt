package org.lmd.project.delivery.domain.repository

import org.lmd.project.delivery.domain.model.DeliveryLog
import org.lmd.project.delivery.domain.model.Page

interface DeliveriesLogRepository {
    suspend fun getLogsPage(
        page: Int,
        limit: Int,
        statusIds: List<Int>,
        search: String? = null,
    ): Page<DeliveryLog>
}