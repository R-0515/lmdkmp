package org.example.project.myPool.ui.model

import org.example.project.myPool.domian.model.OrderActions
import org.example.project.myPool.domian.model.OrderInfo

data class MapOverlayCallbacks(
    val onBottomHeightMeasured: (Int) -> Unit,
    val onCenteredOrderChange: (OrderInfo, Int) -> Unit,
    val onOpenOrderDetails: (String) -> Unit,
    val onNearEnd: (Int) -> Unit,
)

data class MyOrderCardCallbacks(
    val onDetails: () -> Unit,
    val onCall: () -> Unit,
    val onAction: (OrderActions) -> Unit,
    val onReassignRequested: () -> Unit,
)