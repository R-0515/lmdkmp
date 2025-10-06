package org.example.project.generalPool.domain.mapper

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.example.project.generalPool.domain.model.OrderInfo
import org.example.project.generalPool.domain.model.OrderStatus
import org.example.project.generalPool.domain.model.RelativeTime
import org.lmd.project.socket.Order
import kotlin.math.abs
import kotlin.time.ExperimentalTime

private const val MINUTE_MS = 60_000L
private const val HOUR_MS = 3_600_000L
private const val DAY_MS = 86_400_000L

@OptIn(ExperimentalTime::class)
private fun parseToEpochMillis(s: String?): Long? {
    if (s.isNullOrBlank()) return null

    // 1) ISO/RFC3339
    runCatching { return Instant.parse(s).toEpochMilliseconds() }

    // 2) "yyyy-MM-dd HH:mm:ss" -> "yyyy-MM-ddTHH:mm:ssZ" (assume UTC)
    val dt = Regex("""^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$""")
    if (dt.matches(s)) {
        return runCatching { Instant.parse(s.replace(" ", "T") + "Z").toEpochMilliseconds() }
            .getOrNull()
    }

    // 3) "yyyy-MM-dd" -> midnight UTC
    val d = Regex("""^\d{4}-\d{2}-\d{2}$""")
    if (d.matches(s)) {
        return runCatching {
            LocalDate.parse(s).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        }.getOrNull()
    }

    return null
}

@OptIn(ExperimentalTime::class)
private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()

private fun formatRelative(then: Long?): RelativeTime {
    if (then == null) return RelativeTime.Unknown
    val diff = abs(nowMillis() - then)
    return when {
        diff < MINUTE_MS -> RelativeTime.JustNow
        diff < HOUR_MS -> RelativeTime.MinutesAgo((diff / MINUTE_MS).toInt())
        diff < DAY_MS -> RelativeTime.HoursAgo((diff / HOUR_MS).toInt())
        else -> RelativeTime.DaysAgo((diff / DAY_MS).toInt())
    }
}

fun Order.toUi(): OrderInfo {
    val lat = coordinates?.lat ?: latitude ?: 0.0
    val lng = coordinates?.lng ?: longitude ?: 0.0

    val whenMillis =
        parseToEpochMillis(lastUpdated)
            ?: parseToEpochMillis(orderDate)
            ?: parseToEpochMillis(deliveryTime)

    val timeAgo = formatRelative(whenMillis)

    return OrderInfo(
        id = orderId ?: id ?: orderNumber ?: "-",
        name = customerName ?: "-",
        orderNumber = orderNumber ?: orderId ?: id ?: "-",
        status = OrderStatus.fromId(statusId),
        assignedAgentId = assignedAgentId,
        timeAgo = timeAgo,
        itemsCount = 0,
        distanceKm = Double.POSITIVE_INFINITY,
        lat = lat,
        lng = lng,
        customerPhone = phone,
        details = address,
    )
}
