package org.example.project.myPool.ui.viewmodel

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import org.example.project.socket.Coordinates
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.ui.state.MyOrdersUiState

class OrdersStore(
    val state: MutableStateFlow<MyOrdersUiState>,
    val currentUserId: MutableStateFlow<String?>,
    val deviceLocation: MutableStateFlow<Location?>,
    val allOrders: MutableList<OrderInfo> = mutableListOf(),
) {
    var page: Int = 1
    var endReached: Boolean = false
}
