package org.example.project.orderhistory.domain.model

data class OrdersHistoryFilter(
    val allowed: Set<OrderHistoryStatus> = setOf(
        OrderHistoryStatus.CANCELLED,
        OrderHistoryStatus.FAILED,
        OrderHistoryStatus.DONE,
        OrderHistoryStatus.UNKNOWN,
    ),
    val ageAscending: Boolean = false,
)