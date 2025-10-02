package org.example.project.myPoolMyOrder.screen.model

import androidx.compose.foundation.lazy.LazyListState
import org.example.project.myPool.domian.model.OrderInfo

//OrderList
data class OrderListState(
    val orders: List<OrderInfo>,
    val listState: LazyListState,
    val isLoadingMore: Boolean,
    val updatingIds: Set<String>,
    val isRefreshing: Boolean,
    val endReached: Boolean,
)