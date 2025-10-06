package org.example.project.myPool.ui.logic

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.domian.util.OrdersPaging
import org.example.project.myPool.ui.model.MyOrdersPoolUiState
import org.example.project.myPool.ui.model.MyPoolLoadResult
import org.example.project.socket.Coordinates

class MyPoolLogic(
    private val getMyOrders: GetMyOrdersUseCase,
    private val computeDistancesUseCase: ComputeDistancesUseCase,
    private val deviceLocation: MutableStateFlow<Coordinates?>,
    private val scope: CoroutineScope,
) {
    private val _ui = MutableStateFlow(MyOrdersPoolUiState())
    val ui: StateFlow<MyOrdersPoolUiState> = _ui

    private var page = 1
    private val pageSize = OrdersPaging.PAGE_SIZE
    private var loadingJob: Job? = null

    init {
        refresh()
    }

    fun updateDeviceLocation(coords: Coordinates?) {
        deviceLocation.value = coords
        if (coords != null && _ui.value.orders.isNotEmpty()) {
            val computed = computeDistancesUseCase(coords, _ui.value.orders)
            _ui.update { it.copy(orders = computed, hasLocationPerm = true) }
        }
    }

    fun refresh() {
        loadingJob?.cancel()
        loadingJob = scope.launch { fillFirstChunk() }
    }

    fun onClear() {
        loadingJob?.cancel(CancellationException("Logic cleared"))
        loadingJob = null
    }

    private suspend fun fillFirstChunk() {
        _ui.update {
            it.copy(isLoading = true, isLoadingMore = false, endReached = false, orders = emptyList())
        }

        val acc = ArrayList<OrderInfo>(pageSize)
        val result = runCatching {
            fillPagesForInitial(
                pageSize = pageSize,
                fetch = { p, l -> getMyOrders(page = p, limit = l, userOrdersOnly = true) },
                acc = acc,
            )
        }

        result.onFailure {
            _ui.update { it.copy(isLoading = false) }
            return
        }

        val (reachedEnd, lastPage) = result.getOrThrow()
        applyInitialResult(acc, reachedEnd, lastPage)
    }

    private fun applyInitialResult(
        acc: List<OrderInfo>,
        reachedEnd: Boolean,
        lastPage: Int,
    ) {
        val merged = mergeById(emptyList(), acc)
        val loc = deviceLocation.value
        val computed = if (loc != null) computeDistancesUseCase(loc, merged) else merged

        page = lastPage.coerceAtLeast(1)
        _ui.update {
            it.copy(
                isLoading = false,
                isLoadingMore = false,
                orders = computed,
                endReached = reachedEnd && acc.isEmpty(),
            )
        }
    }

    fun loadNextIfNeeded(currentIndex: Int) {
        val state = _ui.value
        if (state.isLoading || state.isLoadingMore || state.endReached) return
        if (currentIndex < state.orders.size - NEAR_END_THRESHOLD) return

        loadingJob?.cancel()
        loadingJob = scope.launch { doPagedAppend() }
    }

    private suspend fun doPagedAppend() {
        _ui.update { it.copy(isLoadingMore = true) }
        val startPage = page + 1
        when (val r = loadUntilAppend(startPage)) {
            is MyPoolLoadResult.Appended -> Unit
            is MyPoolLoadResult.EndReached ->
                _ui.update { it.copy(isLoadingMore = false, endReached = true) }
            is MyPoolLoadResult.NoChange ->
                _ui.update { it.copy(isLoadingMore = false, endReached = false) }
            is MyPoolLoadResult.Error ->
                _ui.update { it.copy(isLoadingMore = false) }
        }
    }

    private suspend fun loadUntilAppend(startPage: Int): MyPoolLoadResult {
        var pageAt = startPage
        var hops = 0
        var outcome: MyPoolLoadResult = MyPoolLoadResult.NoChange(pageAt)
        var done = false

        while (!done && hops < PREFETCH_AHEAD_PAGES) {
            val attempt = runCatching {
                getMyOrders(page = pageAt, limit = pageSize, userOrdersOnly = true)
            }
            outcome = attempt.fold(
                onSuccess = { res ->
                    handleSuccessPaging(
                        res = res,
                        pageAt = pageAt,
                        pageSize = pageSize,
                        append = { items, rawCount, p -> applyAppend(items, rawCount, p) },
                    )
                },
                onFailure = { handleErrorPaging(it) },
            )

            done = outcome !is MyPoolLoadResult.NoChange
            if (outcome is MyPoolLoadResult.NoChange) {
                pageAt++; hops++
            }
        }
        return outcome
    }

    private fun applyAppend(
        items: List<OrderInfo>,
        rawCount: Int,
        curPage: Int,
    ) {
        val merged = mergeById(_ui.value.orders, items)
        val loc = deviceLocation.value
        val computed = if (loc != null) computeDistancesUseCase(loc, merged) else merged

        page = curPage
        _ui.update {
            it.copy(
                isLoadingMore = false,
                orders = computed,
                endReached = rawCount < pageSize,
            )
        }
    }

    fun onCenteredOrderChange(order: OrderInfo, index: Int = 0) {
        _ui.update { it.copy(selectedOrderNumber = order.orderNumber) }
        loadNextIfNeeded(index)
    }

    companion object {
        const val NEAR_END_THRESHOLD = 2
        private const val PREFETCH_AHEAD_PAGES = 3
    }
}
