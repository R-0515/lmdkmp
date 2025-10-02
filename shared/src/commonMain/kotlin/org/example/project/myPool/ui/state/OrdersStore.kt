package org.example.project.myPool.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import org.example.project.location.domain.model.Coordinates
import org.example.project.myPool.domian.model.OrderInfo

class OrdersStore(
    val state: MutableStateFlow<MyOrdersUiState>,
    val currentUserId: MutableStateFlow<String?>,
    val deviceLocation: MutableStateFlow<Coordinates?>,
    val allOrders: MutableList<OrderInfo> = mutableListOf(),
) {
    var page: Int = 1
    var endReached: Boolean = false
}