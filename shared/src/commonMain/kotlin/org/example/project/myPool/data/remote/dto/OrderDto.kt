package org.example.project.myPool.data.remote.dto

import org.example.project.location.domain.model.Coordinates


@Serializable
data class OrderDto(
    @SerialName("order_id") val orderId: String,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("customer_name") val customerName: String? = null,
    val address: String? = null,
    @SerialName("status_id") val statusId: Int? = null,
    @SerialName("assigned_agent_id") val assignedAgentId: String? = null,
    @SerialName("price") val price: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("partner_id") val partnerId: String? = null,
    @SerialName("dc_id") val dcId: String? = null,
    @SerialName("order_date") val orderDate: String? = null,
    @SerialName("delivery_time") val deliveryTime: String? = null,
    @SerialName("sla_met") val slaMet: String? = null,
    @SerialName("serial_number") val serialNumber: String? = null,
    val coordinates: Coordinates? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
    val orderstatuses: OrderStatusDto? = null,
    val users: UserDto? = null,
    val partners: Any? = null,
    val distributioncenters: Any? = null,
    @SerialName("distance_km") val distanceKm: Double? = null,
)

@Serializable
data class CoordinatesDto(
    val latitude: Double,
    val longitude: Double
)
