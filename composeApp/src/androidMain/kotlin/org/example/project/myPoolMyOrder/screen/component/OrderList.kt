package org.example.project.myPoolMyOrder.screen.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.ui.model.MyOrderCardCallbacks
import org.example.project.myPool.ui.model.OrderListCallbacks
import org.example.project.myPoolMyOrder.screen.model.OrderListState
import org.example.project.myPool.ui.state.AutoHideOnSuccessStatuses
import org.example.project.myPool.ui.viewmodel.UpdateOrderStatusViewModel
import org.example.project.myPoolMyOrder.screen.component.list.PagingState
import org.example.project.myPoolMyOrder.screen.component.list.defaultVerticalListConfig
import org.example.project.myPoolMyOrder.screen.component.list.verticalListComponent

@Composable
fun orderList(
    state: OrderListState,
    updateVm: UpdateOrderStatusViewModel,
    callbacks: OrderListCallbacks,
) {
    val filteredOrders = rememberFilteredOrders(state, updateVm)

    Box(Modifier.padding(top = 12.dp)) {
        verticalListComponent(
            items = filteredOrders,
            key = { it.id },
            itemContent = { order ->
                myOrderCard(
                    order = order,
                    isUpdating = state.updatingIds.contains(order.id),
                    callbacks =
                        MyOrderCardCallbacks(
                            onReassignRequested = { callbacks.onReassignRequested(order.id) },
                            onDetails = { callbacks.onDetails(order.id) },
                            onCall = { callbacks.onCall(order.id) },
                            onAction = { act -> callbacks.onAction(order.id, act) },
                        ),
                    updateVm = updateVm,
                )
            },
            config =
                defaultVerticalListConfig(
                    listState = state.listState,
                    paging =
                        PagingState(
                            isRefreshing = state.isRefreshing,
                            onRefresh = callbacks.onRefresh,
                            isLoadingMore = state.isLoadingMore,
                            endReached = state.endReached,
                            onLoadMore = callbacks.onLoadMore,
                        ),
                ),
        )
    }
}

@Composable
private fun rememberFilteredOrders(
    state: OrderListState,
    updateVm: UpdateOrderStatusViewModel,
): List<OrderInfo> {
    var hiddenIds by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(updateVm) {
        updateVm.success.collect { s ->
            if (s.status in AutoHideOnSuccessStatuses) hiddenIds = hiddenIds + s.id
        }
    }
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) hiddenIds = emptySet()
    }

    val filtered by remember(state.orders, hiddenIds) {
        derivedStateOf { state.orders.filter { it.id !in hiddenIds } }
    }
    return filtered
}