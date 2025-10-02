package org.example.project.generalPool.domain.model

interface MapUiState {
    val mapOrders: List<OrderInfo>
    val selected: OrderInfo?
    val distanceThresholdKm: Double
    val hasLocationPerm: Boolean
}