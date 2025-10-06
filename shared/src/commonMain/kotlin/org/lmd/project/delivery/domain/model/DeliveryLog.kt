package org.lmd.project.delivery.domain.model

data class DeliveryLog(
    val number: String,
    val createdAt: String,
    val deliveryEtaText: String?,
    val state: DeliveryState,
)
data class Page<T>(
    val items: List<T>,
    val hasNext: Boolean,
)
