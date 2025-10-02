package org.example.project.myPoolMyOrder.screen.model

import androidx.compose.ui.unit.Dp
import org.example.project.map.domain.model.IMapStates
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.ui.model.MyOrdersPoolUiState

data class MapOverlayState(
    val isLoading: Boolean,
    val isLoadingMore: Boolean,
    val orders: List<OrderInfo>,
    val bottomPadding: Dp,
    val mapUi: MyOrdersPoolUiState,
    val mapStates: IMapStates,
)