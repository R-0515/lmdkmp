package org.example.project.myPoolMyOrder.screen.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.example.project.R
import org.example.project.myPool.domian.model.OrderActions
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.ui.model.LocalUiOnlyStatusBus
import org.example.project.myPool.ui.model.OrderListCallbacks
import org.example.project.myPoolMyOrder.screen.model.OrderListState
import org.example.project.myPool.ui.model.OrdersContentCallbacks
import org.example.project.myPoolMyOrder.screen.model.OrdersContentDeps
import org.example.project.myPool.ui.state.MyOrdersUiState
import org.example.project.myPool.ui.viewmodel.MyOrdersViewModel
import org.example.project.myPool.ui.viewmodel.UpdateOrderStatusViewModel

@Composable
fun ordersContent(
    ordersVm: MyOrdersViewModel,
    deps: OrdersContentDeps,
    cbs: OrdersContentCallbacks,
    modifier: Modifier = Modifier,
) {
    val uiState by ordersVm.uiState.collectAsState()
    val context = LocalContext.current

    Column(modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.orders.isEmpty() -> loadingView()
            uiState.errorMessage != null ->
                errorView(uiState.errorMessage!!) { ordersVm.listVM.retry(context) }

            uiState.emptyMessage != null -> emptyView(uiState.emptyMessage!!)
            else ->
                orderList(
                    state = buildOrderListState(uiState, deps),
                    updateVm = deps.updateVm,
                    callbacks =
                        buildOrderListCallbacks(
                            uiState = uiState,
                            deps = deps,
                            cbs = cbs,
                            context = context,
                            ordersVm = ordersVm,
                        ),
                )
        }
    }
}


private fun buildOrderListState(
    ui: MyOrdersUiState,
    deps: OrdersContentDeps,
) = OrderListState(
    orders = ui.orders,
    listState = deps.listState,
    isLoadingMore = ui.isLoadingMore,
    updatingIds = deps.updatingIds,
    isRefreshing = ui.isRefreshing,
    endReached = ui.endReached,
)

private fun buildOrderListCallbacks(
    uiState: MyOrdersUiState,
    deps: OrdersContentDeps,
    cbs: OrdersContentCallbacks,
    context: Context,
    ordersVm: MyOrdersViewModel,
) = OrderListCallbacks(
    onReassignRequested = cbs.onReassignRequested,
    onDetails = cbs.onOpenOrderDetails,
    onCall = { id ->
        val phone = uiState.orders.firstOrNull { it.id == id }?.customerPhone
        if (!phone.isNullOrBlank()) {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        } else {
            LocalUiOnlyStatusBus.errorEvents
                .tryEmit(context.getString(R.string.phone_missing) to null)
        }
    },
    onAction = { orderId, dialog ->
        val order = uiState.orders.firstOrNull { it.id == orderId }
        val label =
            when (dialog) {
                OrderActions.Confirm -> "Confirm"
                OrderActions.PickUp -> "PickUp"
                OrderActions.Start -> "StartDelivery"
                OrderActions.Deliver -> "Deliver"
                OrderActions.Fail -> "DeliveryFailed"
            }
        UpdateOrderStatusViewModel.OrderLogger.uiTap(orderId, order?.orderNumber, label)
        when (dialog) {
            OrderActions.Confirm -> deps.updateVm.update(orderId, OrderStatus.CONFIRMED)
            OrderActions.PickUp -> deps.updateVm.update(orderId, OrderStatus.PICKUP)
            OrderActions.Start -> deps.updateVm.update(orderId, OrderStatus.START_DELIVERY)
            OrderActions.Deliver -> deps.updateVm.update(orderId, OrderStatus.DELIVERY_DONE)
            OrderActions.Fail -> deps.updateVm.update(orderId, OrderStatus.DELIVERY_FAILED)
        }
    },
    onRefresh = { ordersVm.listVM.refresh(context) },
    onLoadMore = { ordersVm.listVM.loadNextPage(context) },
)