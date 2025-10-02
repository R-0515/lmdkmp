package org.example.project.myPool.ui.viewmodel

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import org.example.project.myPool.ui.logic.myOrderLogic.OrdersListControllerCore
import org.example.project.myPool.ui.model.OrdersListControllerDeps

class OrdersListController(
    deps: OrdersListControllerDeps,
    scope: CoroutineScope,
) {
    private val core = OrdersListControllerCore(deps, scope)

    fun setCurrentUserId(id: String?) = core.setCurrentUserId(id)

    fun loadInitial(context: Context) =
        core.loadInitial { loadInitial(context) }

    fun refresh(context: Context) =
        core.refresh { refresh(context) }

    fun refreshStrict() = core.refreshStrict()

    fun loadNextPage(context: Context) =
        core.loadNextPage { loadNextPage(context) }
}