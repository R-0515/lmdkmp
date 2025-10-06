package org.example.project.myPoolMyOrder.screen.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import org.example.project.R
import org.example.project.myPool.domian.model.OrderActions
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.ui.model.MyOrderCardCallbacks
import org.example.project.myPool.ui.viewmodel.UpdateOrderStatusViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun myPoolOrderCardItem(
    order: OrderInfo,
    onOpenOrderDetails: (String) -> Unit,
    onCall: (String?) -> Unit,
) {
    Box(Modifier.width(dimensionResource(R.dimen.myOrders_card_width))) {
        val updateVm: UpdateOrderStatusViewModel = koinViewModel()
        myOrderCard(
            order = order,
            isUpdating = false,
            callbacks =
                MyOrderCardCallbacks(
                    onReassignRequested = {},
                    onDetails = { onOpenOrderDetails(order.orderNumber) },
                    onCall = { onCall(order.customerPhone) },
                    onAction = { action: OrderActions -> },
                ),
            updateVm = updateVm,
        )
    }
}