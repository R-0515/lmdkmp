package org.example.project.myPool.ui.model

data class OrdersContentCallbacks(
    val onOpenOrderDetails: (String) -> Unit,
    val onReassignRequested: (String) -> Unit,
)