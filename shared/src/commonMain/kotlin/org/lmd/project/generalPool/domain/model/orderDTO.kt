package org.lmd.project.generalPool.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrdersEnvelope(
    val success: Boolean,
    val data: OrdersData? = null,
    val error: String? = null,
)

@Serializable
data class OrdersData(
    val orders: List<OrderDto> = emptyList(),
    val pagination: PaginationDto? = null,
)

@Serializable
data class PaginationDto(
    @SerialName("current_page") val currentPage: Int? = null,
    @SerialName("total_pages") val totalPages: Int? = null,
    @SerialName("total_count") val totalCount: Int? = null,
    val limit: Int? = null,
    @SerialName("has_next_page") val hasNextPage: Boolean? = null,
    @SerialName("has_prev_page") val hasPrevPage: Boolean? = null,
)

@Serializable
data class OrderDto(
    @SerialName("order_id") val orderId: String,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    val address: String? = null,
    @SerialName("status_id") val statusId: Int? = null,
    @SerialName("assigned_agent_id") val assignedAgentId: String? = null,
    val price: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("partner_id") val partnerId: String? = null,
    @SerialName("dc_id") val dcId: String? = null,
    @SerialName("order_date") val orderDate: String? = null,
    @SerialName("delivery_time") val deliveryTime: String? = null,
    @SerialName("sla_met") val slaMet: String? = null,
    @SerialName("serial_number") val serialNumber: String? = null,
    val coordinates: CoordinatesDto? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
    val orderstatuses: OrderStatusDto? = null,
    val users: UserDto? = null,
    val partners: String? = null,              // or appropriate type
    val distributioncenters: String? = null,  // or appropriate type
    @SerialName("distance_km") val distanceKm: Double? = null,
)

@Serializable
data class CoordinatesDto(
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class OrderStatusDto(
    @SerialName("color_code") val colorCode: String? = null,
    @SerialName("status_name") val statusName: String? = null,
    @SerialName("font_color_code") val fontColorCode: String? = null,
)

@Serializable
data class UserDto(
    val id: String? = null,
    val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
)
