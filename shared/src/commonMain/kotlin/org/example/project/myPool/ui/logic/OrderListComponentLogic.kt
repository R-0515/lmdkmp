package org.example.project.myPool.ui.logic

import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.flow.update
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.domian.util.OrdersPaging
/**
 * Pure shared KMP logic (safe for both Android & iOS)
 */

class OrdersPager(
    private val getMyOrders: GetMyOrdersUseCase,
) {
    suspend fun getPage(
        page: Int,
        bypassCache: Boolean,
        assignedAgentId: String? = null,
        userOrdersOnly: Boolean = true,
        limit: Int = OrdersPaging.PAGE_SIZE,
    ): List<OrderInfo> {
        val res = getMyOrders(
            page = page,
            limit = limit,
            bypassCache = bypassCache,
            assignedAgentId = assignedAgentId,
            userOrdersOnly = userOrdersOnly,
        )
        return res.items
    }
}

class OrdersThrottle(
    private val cooldownMs: Long = 5_000L,
) {
    private var lastNextPageErrorAtMs: Long = 0L
    private var lastFailedPage: Int? = null

    fun canRequest(page: Int): Boolean {
        val withinCooldown =
            lastFailedPage == page &&
                    (currentTimeMillis() - lastNextPageErrorAtMs) < cooldownMs
        return !withinCooldown
    }

    fun markError(page: Int) {
        lastFailedPage = page
        lastNextPageErrorAtMs = currentTimeMillis()
    }

    fun clear() {
        lastFailedPage = null
        lastNextPageErrorAtMs = 0L
    }

    private fun currentTimeMillis(): Long = getTimeMillis()
}

class OrdersListPublisher(
    private val store: OrdersStore,
    private val helpers: OrdersListHelpersCommon,
) {
    private val state get() = store.state
    private val deviceLocation get() = store.deviceLocation
    private val allOrders get() = store.allOrders
    private val currentUserId get() = store.currentUserId

    fun setCurrentUserIdAndRecompute(id: String?) {
        currentUserId.value = id
        val display = helpers.computeDisplay(deviceLocation.value, allOrders, state.value.query, id)
        state.update { it.copy(orders = display) }
    }

    fun publishFirstPage(
        items: List<OrderInfo>,
        endReached: Boolean,
    ) {
        val uid = currentUserId.value
        val display = helpers.computeDisplay(deviceLocation.value, items, state.value.query, uid)
        helpers.publishFirstPageFrom(
            state = state,
            base = display,
            pageSize = OrdersPaging.PAGE_SIZE,
            query = state.value.query,
            endReached = endReached,
        )
    }

    fun publishAppend() {
        val uid = currentUserId.value
        val display = helpers.computeDisplay(deviceLocation.value, store.allOrders, state.value.query, uid)
        helpers.publishAppendFrom(
            state = state,
            base = display,
            page = store.page,
            pageSize = OrdersPaging.PAGE_SIZE,
            endReached = store.endReached,
        )
    }
}