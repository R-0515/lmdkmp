package org.example.project.socket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames


@Serializable
data class Order(
    @SerialName("id") val id: String? = null,

    @SerialName("order_id") @JsonNames("order_id") val orderId: String? = null,

    @SerialName("order_number") @JsonNames("order_number") val orderNumber: String? = null,

    @SerialName("customer_name") @JsonNames("customer_name") val customerName: String? = null,

    @SerialName("address") val address: String? = null,

    @SerialName("status_id") val statusId: Int? = null,

    @SerialName("assigned_agent_id") val assignedAgentId: String? = null,

    @SerialName("price") val price: String? = null,

    @SerialName("phone") val phone: String? = null,

    @SerialName("partner_id") val partnerId: String? = null,

    @SerialName("dc_id") val dcId: String? = null,
    @SerialName("order_date") @JsonNames("order_date", "orderDate")
    val orderDate: String? = null,

    @SerialName("delivery_time") @JsonNames("delivery_time", "deliveryTime")
    val deliveryTime: String? = null,

    @SerialName("last_updated") @JsonNames("last_updated", "updatedAt", "lastUpdated")
    val lastUpdated: String? = null,

//    @SerialName("coordinates") val coordinates: Coordinates,
    @SerialName("coordinates") val coordinates: Coordinates? = null,

    @SerialName("latitude") @JsonNames("latitude", "lat")
    val latitude: Double? = null,

    @SerialName("longitude") @JsonNames("longitude", "lng", "lon")
    val longitude: Double? = null,
)
@Serializable
data class Coordinates(
    @SerialName("lat") @JsonNames("lat", "latitude") val lat: Double? = null,
    @SerialName("lng") @JsonNames("lng", "lon", "longitude") val lng: Double? = null,
)