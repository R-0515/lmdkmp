package org.example.project.myPool.ui.model

import org.example.project.myPool.domian.model.OrderActions

data class OrderListCallbacks(
    val onReassignRequested: (String) -> Unit,
    val onDetails: (String) -> Unit,
    val onCall: (String) -> Unit,
    val onAction: (String, OrderActions) -> Unit,
    val onRefresh: () -> Unit,
    val onLoadMore: () -> Unit,
)