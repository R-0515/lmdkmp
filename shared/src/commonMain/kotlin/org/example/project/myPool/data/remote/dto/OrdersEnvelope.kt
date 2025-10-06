package org.example.project.myPool.data.remote.dto

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