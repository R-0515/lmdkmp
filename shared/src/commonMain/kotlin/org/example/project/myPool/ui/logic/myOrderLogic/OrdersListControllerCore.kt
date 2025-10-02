package org.example.project.myPool.ui.logic.myOrderLogic

class OrdersListControllerCore(
    private val deps: OrdersListControllerDeps,
    private val scope: CoroutineScope,
) {
    private val store get() = deps.store
    private val pager get() = deps.pager
    private val publisher get() = deps.publisher
    private val errors get() = deps.errors   // 👉 Now this is OrdersErrorCore, not Android handler
    private val throttle get() = deps.throttle

    private val state get() = store.state
    private val currentUserId get() = store.currentUserId
    private val allOrders get() = store.allOrders

    fun setCurrentUserId(id: String?) = publisher.setCurrentUserIdAndRecompute(id)

    fun loadInitial(retry: () -> Unit) {
        val alreadyHasData = state.value.orders.isNotEmpty()
        if (state.value.isLoading) return

        state.update { it.copy(isLoading = !alreadyHasData, errorMessage = null, emptyMessage = null) }

        scope.launch {
            try {
                val (items, endReached) = fetchFirstPage(bypassCache = true)
                publisher.publishFirstPage(items, endReached)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: IOException) {
                errors.handleInitialErrorMessage(e.toUserMessage(), alreadyHasData, retry)
            } catch (e: HttpException) {
                errors.postError(e.toUserMessage(), retry)
            } catch (e: SSLHandshakeException) {
                errors.postError(e.toUserMessage(), retry)
            } catch (e: UnknownHostException) {
                errors.postError(e.toUserMessage(), retry)
            } finally {
                state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refresh(retry: () -> Unit) {
        if (state.value.isRefreshing) return
        state.update { it.copy(isRefreshing = true, errorMessage = null) }

        scope.launch {
            try {
                val (items, endReached) = fetchFirstPage(bypassCache = true)
                publisher.publishFirstPage(items, endReached)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: IOException) {
                errors.postError(e.toUserMessage(), retry)
            } catch (e: HttpException) {
                errors.postError(e.toUserMessage(), retry)
            } catch (e: SSLHandshakeException) {
                errors.postError(e.toUserMessage(), retry)
            } catch (e: UnknownHostException) {
                errors.postError(e.toUserMessage(), retry)
            } finally {
                state.update { it.copy(isRefreshing = false) }
            }
        }
    }

    fun refreshStrict() {
        if (state.value.isLoading) return
        scope.launch {
            state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { fetchFirstPage(bypassCache = true) }
                .onSuccess { (items, endReached) -> publisher.publishFirstPage(items, endReached) }
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

    fun loadNextPage(retry: () -> Unit) {
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
            } catch (e: IOException) {
                throttle.markError(nextPageNum)
                errors.postError(e.toUserMessage(), retry)
            } catch (e: HttpException) {
                throttle.markError(nextPageNum)
                errors.postError(e.toUserMessage(), retry)
            } catch (e: UnknownHostException) {
                throttle.markError(nextPageNum)
                errors.postError(e.toUserMessage(), retry)
            } finally {
                state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private suspend fun fetchFirstPage(bypassCache: Boolean): Pair<List<OrderInfo>, Boolean> {
        store.page = 1
        store.endReached = false
        allOrders.clear()

        val first =
            pager.getPage(
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