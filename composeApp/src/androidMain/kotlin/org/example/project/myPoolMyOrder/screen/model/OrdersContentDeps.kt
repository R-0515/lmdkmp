package org.example.project.myPoolMyOrder.screen.model

import androidx.compose.foundation.lazy.LazyListState
import org.example.project.myPool.ui.viewmodel.UpdateOrderStatusViewModel


data class OrdersContentDeps(
    val updateVm: UpdateOrderStatusViewModel,
    val listState: LazyListState,
    val updatingIds: Set<String>,
)