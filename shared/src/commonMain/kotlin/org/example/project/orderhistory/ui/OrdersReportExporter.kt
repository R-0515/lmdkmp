package org.example.project.orderhistory.ui

import org.example.project.orderhistory.domain.model.OrderHistoryUi
import org.example.project.orderhistory.domain.model.OrdersHistoryFilter

/** Result of export */
data class ExportResult(
    val ok: Boolean,
    val uriString: String? = null,
    val error: String? = null,
)

/** Platform-specific implementation lives per target (Android actual). */
interface OrdersReportExporter {
    suspend fun export(
        orders: List<OrderHistoryUi>,
        meta: ReportMeta
    ): ExportResult
}

/** Extra info to print in header */
data class ReportMeta(
    val title: String,
    val filterSummary: String,
    val generatedAt: String,
)

/** Common helper to summarize filter state in the PDF header/footer. */
fun OrdersHistoryFilter.toSummary(): String {
    val statuses = if (allowed.isEmpty()) "All statuses" else allowed.joinToString { it.name }
    val sort = if (ageAscending) "Oldest first" else "Newest first"
    return "Filter: $statuses • Sort: $sort"
}
