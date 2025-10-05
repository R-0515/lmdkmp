package org.example.project.orderhistory.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class OrdersListResponse(
    val success: Boolean,
    val data: OrdersData? = null,
    val error: String? = null,
)

@Serializable
data class OrdersData(
    val orders: List<OrderHistoryDto> = emptyList(),
    val pagination: PaginationDto? = null,
)

@Serializable
data class PaginationDto(
    @SerialName("current_page") val currentPage: Int,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_count") val totalCount: Int,
    val limit: Int,
    @SerialName("has_next_page") val hasNextPage: Boolean,
    @SerialName("has_prev_page") val hasPrevPage: Boolean,
)

@Serializable
data class OrderHistoryDto(
    @SerialName("order_id") val orderId: String,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("customer_name") val customerName: String,
    @SerialName("order_date") val orderDate: String,
    @SerialName("last_updated") val lastUpdated: String,
    @SerialName("orderstatuses") val orderStatus: OrderStatusDto,
)

@Serializable
data class OrderStatusDto(
    @SerialName("status_name") val statusName: String,
    @SerialName("color_code") val colorCode: String,
    @SerialName("font_color_code") val fontColorCode: String,
)
data class Paged<T>(
    val items: List<T>,
    val hasNext: Boolean
)