package org.example.project.myPool.ui.viewmodel.myOrder

import androidx.lifecycle.ViewModel
import org.example.project.myPool.domian.util.OrdersPaging
import org.example.project.myPool.ui.logic.myOrderLogic.OrdersSearchLogic
import org.example.project.myPool.ui.state.OrdersStore

class OrdersSearchViewModel(
    store: OrdersStore,
    pageSize: Int = OrdersPaging.PAGE_SIZE
) : ViewModel() {

    private val logic = OrdersSearchLogic(store, pageSize)

    fun applySearchQuery(raw: String) = logic.applySearchQuery(raw)
}