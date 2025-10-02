package org.example.project.myPool.domian.model

data class OrdersPage(
    val items: List<OrderInfo>,
    val rawCount: Int, // server page size BEFORE filtering
)