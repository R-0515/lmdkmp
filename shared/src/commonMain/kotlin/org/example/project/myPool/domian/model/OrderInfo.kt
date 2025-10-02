package org.example.project.myPool.domian.model

data class OrderInfo(
    val id: String = "",
    val name: String = "",
    val orderNumber: String = "",
    val timeAgo: RelativeTime = RelativeTime.Unknown,
    val itemsCount: Int = 0,
    val distanceKm: Double = 0.0,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val status: OrderStatus? = OrderStatus.ADDED,
    val price: String? = null,
    val customerPhone: String? = null,
    val details: String? = null,
    val customerId: String? = "",
    val assignedAgentId: String? = "",
)
