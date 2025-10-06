package org.example.project.myPoolMyOrder.screen.model

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController
import org.example.project.myPool.ui.viewmodel.ActiveAgentsViewModel
import org.example.project.myPool.ui.viewmodel.MyOrdersViewModel
import org.example.project.myPool.ui.viewmodel.MyPoolViewModel
import org.example.project.myPool.ui.viewmodel.UpdateOrderStatusViewModel


data class WireDeps(
    val navController: NavController,
    val ordersVm: MyOrdersViewModel,
    val updateVm: UpdateOrderStatusViewModel,
    val agentsVm: ActiveAgentsViewModel,
    val poolVm: MyPoolViewModel,
    val listState: LazyListState,
    val snack: SnackbarHostState,
    val reassignOrderId: MutableState<String?>,
)