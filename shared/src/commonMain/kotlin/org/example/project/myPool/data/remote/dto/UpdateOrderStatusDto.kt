package org.example.project.myPool.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrderStatusRequest(
    @SerialName("order_id") val orderId: String,
    @SerialName("status_id") val statusId: Int,
    @SerialName("assigned_agent_id") val assignedAgentId: String? = null,
)

@Serializable
data class UpdateOrderStatusEnvelope(
    val success: Boolean,
    val message: String? = null,
    val data: UpdatedOrderData? = null,
    @SerialName("updated_by") val updatedBy: UpdatedByDto? = null,
)

@Serializable
data class UpdatedOrderData(
    @SerialName("order_number") val orderNumber: String? = null,
    @SerialName("order_id") val orderId: String,
    @SerialName("status_id") val statusId: Int,
    @SerialName("assigned_agent_id") val assignedAgentId: String? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    val address: String? = null,
    @SerialName("orderstatuses") val orderStatuses: OrderStatusDto? = null,
    @SerialName("assigned_agent") val assignedAgent: AssignedAgentDto? = null,
    @SerialName("previous_status_id") val previousStatusId: Int? = null,
    @SerialName("status_changed") val statusChanged: Boolean? = null,
)

@Serializable
data class AssignedAgentDto(
    @SerialName("id") val id: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
)

@Serializable
data class UpdatedByDto(
    @SerialName("user_id") val userId: String? = null,
    val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
)