package org.example.project.delivery.ui.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.delivery.domain.model.DeliveryLog
import org.example.project.delivery.domain.usecase.DeliveryStatusIds
import org.example.project.delivery.domain.usecase.GetDeliveriesLogPageUseCase

object OrdersPaging {
    const val PAGE_SIZE = 20
}

object DeliveryStatusIds {
    const val CANCELLED = 3
    const val FAILED = 7
    const val DELIVERED = 8
    val DEFAULT_LOG_STATUSES: List<Int> = listOf(CANCELLED, FAILED, DELIVERED)
}

class DeliveriesLogSharedViewModel(
    private val scope: CoroutineScope,
    private val getLogsUseCase: GetDeliveriesLogPageUseCase,
) {
    // region === State ===
    private val _logs = MutableStateFlow<List<DeliveryLog>>(emptyList())
    val logs: StateFlow<List<DeliveryLog>> = _logs

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _endReached = MutableStateFlow(false)
    val endReached: StateFlow<Boolean> = _endReached

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    // endregion

    // region === Paging control ===
    private var currentPage = 1
    private var hasNext = true
    private var currentQuery: String? = null
    private var lastRequested: Pair<Int, String?>? = null
    private var generationCounter = 0
    // endregion

    /** 🔹 Load first page */
    fun load() {
        scope.launch {
            _isRefreshing.value = true
            resetPaging()
            try {
                fetchNext()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /** 🔹 Refresh manually */
    fun refresh() {
        if (_isRefreshing.value) return
        scope.launch {
            _isRefreshing.value = true
            resetPaging()
            try {
                fetchNext()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /** 🔹 Load next page if available */
    fun loadMore() {
        if (_isLoadingMore.value || _endReached.value || _isRefreshing.value) return
        scope.launch {
            fetchNext()
        }
    }

    /** 🔹 Search by Order ID or keyword */
    fun searchById(query: String) {
        val normalized = query.trim().removePrefix("#").ifEmpty { null }
        if (normalized != currentQuery) {
            currentQuery = normalized
            refresh()
        }
    }

    /** 🔹 Called from LazyColumn to trigger pagination */
    fun loadMoreIfNeeded(lastVisibleIndex: Int) {
        if (_isLoadingMore.value || _endReached.value || _isRefreshing.value) return
        val size = _logs.value.size
        if (lastVisibleIndex >= size - 3) {
            loadMore()
        }
    }

    private fun resetPaging() {
        currentPage = 1
        hasNext = true
        lastRequested = null
        generationCounter += 1
        _endReached.value = false
        _logs.value = emptyList()
    }

    private suspend fun fetchNext() {
        if (!prepareNext()) return
        val gen = generationCounter
        _isLoadingMore.value = true
        try {
            val result = getLogsUseCase(
                page = currentPage,
                limit = OrdersPaging.PAGE_SIZE,
                statusIds = DeliveryStatusIds.DEFAULT_LOG_STATUSES,
                search = currentQuery,
            )

            result.onSuccess { pageData ->
                if (gen != generationCounter) return@onSuccess
                applyPageResult(pageData.items, pageData.hasNext)
                println("page=${currentPage} size=${pageData.items.size} hasNext=${pageData.hasNext}")
            }.onFailure { error ->
                println(" DeliveriesLogSharedViewModel page=$currentPage limit=${OrdersPaging.PAGE_SIZE} error: ${error.message}")
                _endReached.value = true
            }
        } finally {
            _isLoadingMore.value = false
        }
    }

    private fun prepareNext(): Boolean {
        if (!hasNext) {
            _endReached.value = true
            return false
        }
        val key = currentPage to currentQuery
        if (lastRequested == key) return false
        lastRequested = key
        return true
    }

    private fun applyPageResult(items: List<DeliveryLog>, next: Boolean) {
        _logs.value = (_logs.value + items).distinctBy { it.number }
        hasNext = next
        _endReached.value = !hasNext
        if (hasNext) currentPage += 1
    }
}
