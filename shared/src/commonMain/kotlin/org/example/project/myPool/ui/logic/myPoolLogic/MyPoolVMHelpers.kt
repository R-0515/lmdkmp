package org.example.project.myPool.ui.logic.myPoolLogic

import org.example.project.myPool.domian.model.OrderInfo


fun mergeById(
    existing: List<OrderInfo>,
    incoming: List<OrderInfo>,
): List<OrderInfo> {
    if (incoming.isEmpty()) return existing
    val map = LinkedHashMap<String, OrderInfo>(existing.size + incoming.size)
    existing.forEach { map[it.orderNumber] = it }
    incoming.forEach { map[it.orderNumber] = it }
    return map.values.toList()
}

/**
 * Loop pages until we accumulate at least pageSize items or hit end.
 */
suspend fun fillPagesForInitial(
    pageSize: Int,
    fetch: suspend (page: Int, limit: Int) -> OrdersPage,
    acc: MutableList<OrderInfo>,
): Pair<Boolean, Int> {
    var curPage = 1
    var reachedEnd = false
    while (acc.size < pageSize && !reachedEnd) {
        val r = fetch(curPage, pageSize)
        if (r.items.isNotEmpty()) acc += r.items
        reachedEnd = r.rawCount < pageSize
        curPage++
    }
    return reachedEnd to (curPage - 1)
}