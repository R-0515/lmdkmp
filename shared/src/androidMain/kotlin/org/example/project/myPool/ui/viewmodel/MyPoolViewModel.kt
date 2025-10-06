package org.example.project.myPool.ui.viewmodel

import android.location.Location
import android.net.http.HttpException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.location.domain.model.ComputeDistancesUseCase
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.domian.util.OrdersPaging
import org.example.project.myPool.ui.model.MyOrdersPoolUiState
import org.example.project.myPool.ui.model.MyPoolLoadResult
import java.io.IOException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

const val NEAR_END_THRESHOLD = 2
private const val PREFETCH_AHEAD_PAGES = 3

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class MyPoolViewModel(
    private val getMyOrders: GetMyOrdersUseCase,
    private val computeDistancesUseCase: ComputeDistancesUseCase,
) : ViewModel() {
    private val _ui = MutableStateFlow(MyOrdersPoolUiState())
    val ui: StateFlow<MyOrdersPoolUiState> = _ui.asStateFlow()

    private val deviceLocation = MutableStateFlow<Location?>(null)
    val lastLocation: StateFlow<Location?> = deviceLocation.asStateFlow()

    private var page = 1
    private val pageSize = OrdersPaging.PAGE_SIZE
    private var loadingJob: Job? = null

    init {
        refresh()
    }

    fun updateDeviceLocation(location: Location?) {
        deviceLocation.value = location
        if (location != null && _ui.value.orders.isNotEmpty()) {
            val computed = computeDistancesUseCase(location, _ui.value.orders)
            _ui.update { it.copy(orders = computed, hasLocationPerm = true) }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun refresh() {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch { fillFirstChunk() }
    }

    override fun onCleared() {
        loadingJob?.cancel(CancellationException("ViewModel cleared"))
        loadingJob = null
        super.onCleared()
    }


    private suspend fun fillFirstChunk() {
        _ui.update {
            it.copy(
                isLoading = true,
                isLoadingMore = false,
                endReached = false,
                orders = emptyList(),
            )
        }

        val acc = ArrayList<OrderInfo>(pageSize)
        val (reachedEnd, lastPage) =
            runCatching {
                fillPagesForInitial(
                    pageSize = pageSize,
                    fetch = { p, l -> getMyOrders(page = p, limit = l, userOrdersOnly = true) },
                    acc = acc,
                )
            }.getOrElse { e ->
                _ui.update { it.copy(isLoading = false) }
                when (e) {
                    is CancellationException -> throw e
                    // Ktor exceptions
                    is ClientRequestException -> // 4xx
                        Log.e("MyPoolVM", "Initial fill failed (4xx): ${e.response.status}", e)
                    is ServerResponseException -> // 5xx
                        Log.e("MyPoolVM", "Initial fill failed (5xx): ${e.response.status}", e)
                    is RedirectResponseException -> // 3xx
                        Log.e("MyPoolVM", "Initial fill redirected: ${e.response.status}", e)
                    is HttpRequestTimeoutException, is SocketTimeoutException ->
                        Log.e("MyPoolVM", "Initial fill failed: request timed out", e)
                    is UnknownHostException ->
                        Log.e("MyPoolVM", "Initial fill failed: no internet", e)

                    is IOException ->
                        Log.e("MyPoolVM", "Initial fill failed (IO): ${e.message}", e)

                    else ->
                        Log.e("MyPoolVM", "Initial fill failed: ${e.message}", e)
                }
                return
            }

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
        // inline shouldSkipLoad (removes one function from class)
        if (state.isLoading || state.isLoadingMore || state.endReached) return
        if (currentIndex < state.orders.size - NEAR_END_THRESHOLD) return

        loadingJob?.cancel()
        loadingJob = viewModelScope.launch { doPagedAppend() }
    }

    private suspend fun doPagedAppend() {
        _ui.update { it.copy(isLoadingMore = true) }
        val startPage = page + 1
        when (val r = loadUntilAppend(startPage)) {
            is MyPoolLoadResult.Appended -> Unit
            is MyPoolLoadResult.EndReached -> {
                page = r.pageAt
                _ui.update { it.copy(isLoadingMore = false, endReached = true) }
            }

            is MyPoolLoadResult.NoChange -> {
                page = r.pageAt
                _ui.update { it.copy(isLoadingMore = false, endReached = false) }
            }

            is MyPoolLoadResult.Error -> {
                _ui.update { it.copy(isLoadingMore = false) }
                Log.e("MyPoolVM", "Paging load failed: ${r.throwable.message}", r.throwable)
            }
        }
    }

    private suspend fun loadUntilAppend(startPage: Int): MyPoolLoadResult {
        var pageAt = startPage
        var hops = 0
        var outcome: MyPoolLoadResult = MyPoolLoadResult.NoChange(pageAt)
        var done = false

        while (!done && hops < PREFETCH_AHEAD_PAGES) {
            val attempt =
                runCatching {
                    getMyOrders(
                        page = pageAt,
                        limit = pageSize,
                        userOrdersOnly = true,
                    )
                }
            outcome =
                attempt.fold(
                    onSuccess = { res ->
                        handleSuccessPaging(
                            res = res,
                            pageAt = pageAt,
                            pageSize = pageSize,
                            append = { items, rawCount, p -> applyAppend(items, rawCount, p) },
                        )
                    },
                    onFailure = { ex -> handleErrorPaging(ex) },
                )

            done = outcome !is MyPoolLoadResult.NoChange
            if (outcome is MyPoolLoadResult.NoChange) {
                pageAt++
                hops++
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

    fun onCenteredOrderChange(
        order: OrderInfo,
        index: Int = 0,
    ) {
        _ui.update { it.copy(selectedOrderNumber = order.orderNumber) }
        loadNextIfNeeded(index)
    }
}
