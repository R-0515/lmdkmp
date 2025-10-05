package org.example.project.orderhistory.domain.model

import kotlinx.datetime.Instant
import org.example.project.orderhistory.data.dto.OrderHistoryDto

const val CANCELLED_CODE = 3
const val FAILED_CODE = 7
const val DONE_CODE = 8

enum class OrderStatusCode(val code: Int) {
    CANCELLED(CANCELLED_CODE),
    FAILED(FAILED_CODE),
    DONE(DONE_CODE);

    companion object {
        fun fromList(statuses: List<OrderStatusCode>): String =
            statuses.joinToString(",") { it.code.toString() }
    }
}
data class OrderHistoryUi(
    val orderId: String,
    val number: String,
    val customer: String,
    val createdAtMillis: Long,
    val status: OrderHistoryStatus,
    val total: Double,
    val statusColor: String = "",
    val isCancelled: Boolean = false,
    val isFailed: Boolean = false,
    val isDelivered: Boolean = false,
)

enum class OrderHistoryStatus { CANCELLED, FAILED, DONE, UNKNOWN }

fun OrderHistoryDto.toUi(): OrderHistoryUi {
    val name = orderStatus.statusName.lowercase()
    return OrderHistoryUi(
        orderId = orderId,
        number = orderNumber,
        customer = customerName,
        createdAtMillis = parseDate(lastUpdated),
        status = when (name) {
            "cancelled", "canceled" -> OrderHistoryStatus.CANCELLED
            "delivery failed" -> OrderHistoryStatus.FAILED
            "delivery done", "delivered" -> OrderHistoryStatus.DONE
            else -> OrderHistoryStatus.UNKNOWN
        },
        total = 0.0,
        statusColor = orderStatus.colorCode,
        isCancelled = name in listOf("cancelled", "canceled"),
        isFailed = name == "delivery failed",
        isDelivered = name in listOf("delivery done", "delivered"),
    )
}

fun parseDate(date: String): Long =
    runCatching { Instant.parse(date).toEpochMilliseconds() }.getOrDefault(0L)
