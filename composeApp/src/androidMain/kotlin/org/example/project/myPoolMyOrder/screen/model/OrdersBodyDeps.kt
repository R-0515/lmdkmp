package org.example.project.myPoolMyOrder.screen.model

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import org.example.project.myPool.ui.viewmodel.ActiveAgentsViewModel
import org.example.project.myPool.ui.viewmodel.MyOrdersViewModel
import org.example.project.myPool.ui.viewmodel.UpdateOrderStatusViewModel

data class OrdersBodyDeps(
    val ordersVm: MyOrdersViewModel,
    val updateVm: UpdateOrderStatusViewModel,
    val agentsVm: ActiveAgentsViewModel,
    val listState: LazyListState,
    val snack: SnackbarHostState,
    val reassignOrderId: MutableState<String?>,
    val onOpenOrderDetails: (String) -> Unit,
)
