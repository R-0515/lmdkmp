package org.example.project.myPool.ui.model

import org.example.project.myPool.ui.logic.myOrderLogic.OrdersListPublisher
import org.example.project.myPool.ui.logic.myOrderLogic.OrdersPager
import org.example.project.myPool.ui.logic.myOrderLogic.OrdersThrottle
import org.example.project.myPool.ui.state.OrdersStore


data class OrdersListControllerDeps(
    val store: OrdersStore,
    val pager: OrdersPager,
    val publisher: OrdersListPublisher,
    val errors: OrdersListErrorHandler,
    val throttle: OrdersThrottle,
)
