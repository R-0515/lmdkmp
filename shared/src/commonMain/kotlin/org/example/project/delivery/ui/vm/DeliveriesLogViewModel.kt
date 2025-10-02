package org.example.project.delivery.ui.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.project.delivery.domain.model.DeliveryLog
import org.example.project.delivery.domain.model.Page
import org.example.project.delivery.domain.usecase.DeliveryStatusIds
import org.example.project.delivery.domain.usecase.GetDeliveriesLogPageUseCase

object OrdersPaging { const val PAGE_SIZE = 20 }

class DeliveriesLogSharedViewModel(
    private val scope: CoroutineScope,
    private val getLogsUseCase: GetDeliveriesLogPageUseCase,
) {
    private val _logs = MutableStateFlow<List<DeliveryLog>>(emptyList())
    val logs: StateFlow<List<DeliveryLog>> = _logs

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _endReached = MutableStateFlow(false)
    val endReached: StateFlow<Boolean> = _endReached

    private val _isRefreshing = MutableStateFlow(true)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private var lastRequested: Pair<Int, String?>? = null
    private var generationCounter = 0

    // paging state
    private var page = 1
    private var hasNext = true
    private var currentQuery: String? = null

    fun load() {
        reset()
        scope.launch {
            _isRefreshing.value = true
            try { fetchNext() } finally { _isRefreshing.value = false }
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return
        scope.launch {
            _isRefreshing.value = true
            try {
                reset()
                fetchNext()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value || _endReached.value) return
        scope.launch { fetchNext() }
    }

    fun searchById(query: String) {
        val normalized = query.trim().removePrefix("#").ifEmpty { null }
        if (normalized != currentQuery) {
            currentQuery = normalized
            refresh()
        }
    }

    // ---- internals ----

    private fun reset() {
        page = 1
        hasNext = true
        lastRequested = null
        generationCounter += 1
        _endReached.value = false
        _logs.value = emptyList()
    }

    private suspend fun fetchNext() {
        if (!prepareNext()) return
        val g = generationCounter
        _isLoadingMore.value = true
        try {
            val result = getLogsUseCase(
                page = page,
                limit = OrdersPaging.PAGE_SIZE,
                statusIds = DeliveryStatusIds.DEFAULT_LOG_STATUSES,
                search = currentQuery,
            )
            if (g != generationCounter) return

            result.onSuccess { pageData: Page<DeliveryLog> ->
                applyPageResult(pageData.items, pageData.hasNext)
            }.onFailure {
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
        val key = page to currentQuery
        if (lastRequested == key) return false
        lastRequested = key
        return true
    }

    private fun applyPageResult(items: List<DeliveryLog>, next: Boolean) {
        _logs.value = (_logs.value + items).distinctBy { it.number } // NOTE: using 'number'
        hasNext = next
        _endReached.value = !hasNext
        if (hasNext) page += 1
    }
}
