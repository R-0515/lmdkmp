package org.example.project.myPool.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.update
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.util.OrdersPaging
import org.example.project.myPool.ui.logic.OrdersSearchLogic
import org.example.project.myPool.ui.logic.OrdersStore

class OrdersSearchViewModel(
    store: OrdersStore,
) : ViewModel() {
    private val logic = OrdersSearchLogic(store)

    fun applySearchQuery(raw: String) = logic.applySearchQuery(raw)
}
