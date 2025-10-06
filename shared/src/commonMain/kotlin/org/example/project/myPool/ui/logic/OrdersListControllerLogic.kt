package org.example.project.myPool.ui.logic

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.util.OrdersPaging
import org.example.project.util.toUserMessage

/**
 * Shared logic version of OrdersListController (no Android imports)
 */
data class OrdersListControllerDeps(
    val store: OrdersStore,
    val pager: OrdersPager,
    val publisher: OrdersListPublisher,
    val throttle: OrdersThrottle,
    //val errors: OrdersListErrorHandler,

    )

class OrdersListController(
    private val deps: OrdersListControllerDeps,
    private val scope: CoroutineScope,
    private val onError: (String, retry: () -> Unit) -> Unit = { _, _ -> },
) {
    private val store get() = deps.store
    private val pager get() = deps.pager
    private val publisher get() = deps.publisher
    private val throttle get() = deps.throttle

    private val state get() = store.state
    private val currentUserId get() = store.currentUserId
    private val allOrders get() = store.allOrders

    fun setCurrentUserId(id: String?) = publisher.setCurrentUserIdAndRecompute(id)

    //  Load the very first page (for first screen open)
    fun loadInitial() {
        if (state.value.isLoading) return

        state.update { it.copy(isLoading = true, errorMessage = null) }

        scope.launch {
            runCatching { fetchFirstPage(bypassCache = true) }
                .onSuccess { (items, endReached) ->
                    publisher.publishFirstPage(items, endReached)
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    val msg = e.toUserMessage()
                    onError(msg) { loadInitial() }
                    state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = msg,
                        )
                    }
                }
        }
    }

    //  Reload data on pull-to-refresh
    fun refresh() {
        if (state.value.isRefreshing) return

        state.update { it.copy(isRefreshing = true, errorMessage = null) }

        scope.launch {
            runCatching { fetchFirstPage(bypassCache = true) }
                .onSuccess { (items, endReached) ->
                    publisher.publishFirstPage(items, endReached)
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    val msg = e.toUserMessage()
                    onError(msg) { refresh() }
                }
                .also {
                    state.update { it.copy(isRefreshing = false) }
                }
        }
    }

    fun refreshStrict() {
        if (state.value.isLoading) return
        scope.launch {
            state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { fetchFirstPage(bypassCache = true) }
                .onSuccess { (items, endReached) ->
                    publisher.publishFirstPage(items, endReached)
                }
                .onFailure { e ->
                    if (e is CancellationException) throw e
                    state.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = e.toUserMessage(),
                        )
                    }
                }
        }
    }

    fun loadNextPage() {
        val s = state.value
        val nextPageNum = store.page + 1

        if (!throttle.canRequest(nextPageNum)) return
        if (s.isLoading || s.isLoadingMore || store.endReached) return

        scope.launch {
            state.update { it.copy(isLoadingMore = true) }
            try {
                val nextItems =
                    pager.getPage(
                        page = nextPageNum,
                        bypassCache = true,
                        assignedAgentId = currentUserId.value,
                        limit = OrdersPaging.PAGE_SIZE,
                    )

                store.endReached = nextItems.isEmpty() || nextItems.size < OrdersPaging.PAGE_SIZE
                store.page = nextPageNum

                allOrders.addAll(nextItems)
                publisher.publishAppend()
                throttle.clear()
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                throttle.markError(nextPageNum)
                onError(e.toUserMessage()) { loadNextPage() }
            } finally {
                state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private suspend fun fetchFirstPage(bypassCache: Boolean): Pair<List<OrderInfo>, Boolean> {
        store.page = 1
        store.endReached = false
        allOrders.clear()

        val first = pager.getPage(
            page = 1,
            bypassCache = bypassCache,
            assignedAgentId = currentUserId.value,
            limit = OrdersPaging.PAGE_SIZE,
        )
        allOrders.addAll(first)
        store.endReached = first.size < OrdersPaging.PAGE_SIZE

        return allOrders.toList() to store.endReached
    }
}