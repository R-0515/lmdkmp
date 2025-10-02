package org.example.project.myPool.domian.model

const val STATUS_ADDED = 1
const val STATUS_CONFIRMED = 2
const val CANCELLED_CODE = 3
const val STATUS_REASSIGNED = 4
const val STATUS_PICKUP = 5
const val STATUS_START_DELIVERY = 6
const val FAILED_CODE = 7
const val DONE_CODE = 8

enum class OrderStatus(val id: Int) {
    ADDED(STATUS_ADDED),
    CONFIRMED(STATUS_CONFIRMED),
    CANCELED(CANCELLED_CODE),
    REASSIGNED(STATUS_REASSIGNED),
    PICKUP(STATUS_PICKUP),
    START_DELIVERY(STATUS_START_DELIVERY),
    DELIVERY_FAILED(FAILED_CODE),
    DELIVERY_DONE(DONE_CODE);

    companion object {
        fun fromId(id: Int?): OrderStatus? = values().firstOrNull { it.id == id }
    }
}

fun OrderStatus.toApiId(): Int =
    when (this) {
        OrderStatus.ADDED -> STATUS_ADDED
        OrderStatus.CONFIRMED -> STATUS_CONFIRMED
        OrderStatus.CANCELED -> CANCELLED_CODE
        OrderStatus.REASSIGNED -> STATUS_REASSIGNED
        OrderStatus.PICKUP -> STATUS_PICKUP
        OrderStatus.START_DELIVERY -> STATUS_START_DELIVERY
        OrderStatus.DELIVERY_FAILED -> FAILED_CODE
        OrderStatus.DELIVERY_DONE -> DONE_CODE
    }