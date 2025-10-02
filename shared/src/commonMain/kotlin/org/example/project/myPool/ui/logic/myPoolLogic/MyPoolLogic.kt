package org.example.project.myPool.ui.logic.myPoolLogic

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.location.domain.model.Coordinates
import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.domian.util.OrdersPaging
import org.example.project.myPool.ui.logic.OrderDistanceHelper
import org.example.project.myPool.ui.model.MyOrdersPoolUiState


class MyPoolLogic(
    private val getMyOrders: GetMyOrdersUseCase,
    private val computeDistancesUseCase: ComputeDistancesUseCase,
    private val scope: CoroutineScope,
) {
    private val _ui = MutableStateFlow(MyOrdersPoolUiState())
    val ui = _ui.asStateFlow()

    private val deviceLocation = MutableStateFlow<Coordinates?>(null)
    val lastLocation = deviceLocation.asStateFlow()

    private val pageSize = OrdersPaging.PAGE_SIZE
    private var page = 1
    private var loadingJob: Job? = null

    private val pager = MyPoolPager(getMyOrders, pageSize)

    init {
        refresh()
    }

    fun updateDeviceLocation(location: Coordinates?) {
        deviceLocation.value = location
        if (location != null && _ui.value.orders.isNotEmpty()) {
            val updated =
                OrderDistanceHelper.applyDistances(
                    origin = location,
                    orders = _ui.value.orders,
                    compute = computeDistancesUseCase::computeDistances,
                )
            _ui.update { it.copy(orders = updated, hasLocationPerm = true) }
        }
    }

    fun refresh() {
        loadingJob?.cancel()
        loadingJob = scope.launch { fillFirstChunk() }
    }

    private suspend fun fillFirstChunk() {
        _ui.update {
            it.copy(isLoading = true, isLoadingMore = false, endReached = false, orders = emptyList())
        }

        val acc = ArrayList<OrderInfo>(pageSize)
        val result =
            runCatching {
                fillPagesForInitial(
                    pageSize = pageSize,
                    fetch = { p, l -> getMyOrders(page = p, limit = l, userOrdersOnly = true) },
                    acc = acc,
                )
            }

        val (reachedEnd, lastPage) =
            result.getOrElse { e ->
                _ui.update { it.copy(isLoading = false, errorMessage = e.message) }
                return
            }

        page = lastPage.coerceAtLeast(1)

        val updated =
            OrderDistanceHelper.applyDistances(
                origin = deviceLocation.value,
                orders = acc,
                compute = computeDistancesUseCase::computeDistances,
            )

        _ui.update {
            it.copy(
                isLoading = false,
                orders = updated,
                isLoadingMore = false,
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

        val result =
            pager.loadUntilAppend(
                startPage = page + 1,
                onAppend = ::applyAppend,
            )

        when (result) {
            is MyPoolPager.LoadResult.Appended -> {
                page = result.pageAt
                _ui.update { it.copy(isLoadingMore = false) }
            }

            is MyPoolPager.LoadResult.EndReached -> {
                page = result.pageAt
                _ui.update { it.copy(isLoadingMore = false, endReached = true) }
            }

            is MyPoolPager.LoadResult.NoChange -> {
                page = result.pageAt
                _ui.update { it.copy(isLoadingMore = false, endReached = false) }
            }

            is MyPoolPager.LoadResult.Error -> {
                _ui.update { it.copy(isLoadingMore = false, errorMessage = result.throwable.message) }
            }
        }
    }

    private fun applyAppend(
        items: List<OrderInfo>,
        rawCount: Int,
        curPage: Int,
    ) {
        val merged = mergeById(_ui.value.orders, items)
        val updated =
            OrderDistanceHelper.applyDistances(
                origin = deviceLocation.value,
                orders = merged,
                compute = computeDistancesUseCase::computeDistances,
            )

        page = curPage
        _ui.update {
            it.copy(
                orders = updated,
                isLoadingMore = false,
                endReached = rawCount < pageSize,
            )
        }
    }

    fun onCenteredOrderChange(order: OrderInfo, index: Int = 0) {
        _ui.update { it.copy(selectedOrderNumber = order.orderNumber) }
        loadNextIfNeeded(index)
    }

    fun clear() {
        loadingJob?.cancel(CancellationException("Logic cleared"))
    }

    companion object {
        private const val NEAR_END_THRESHOLD = 2
    }
}