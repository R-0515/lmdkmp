package org.example.project.delivery.ui.vm


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import org.example.project.delivery.domain.model.DeliveryLog
import org.example.project.delivery.domain.usecase.GetDeliveriesLogPageUseCase

class DeliveriesLogViewModel(
    getLogsUseCase: GetDeliveriesLogPageUseCase,
) : ViewModel() {

    private val shared = DeliveriesLogSharedViewModel(
        scope = viewModelScope,
        getLogsUseCase = getLogsUseCase,
    )

    val logs: StateFlow<List<DeliveryLog>> get() = shared.logs
    val isRefreshing get() = shared.isRefreshing
    val isLoadingMore get() = shared.isLoadingMore
    val endReached get() = shared.endReached

    fun load() = shared.load()
    fun refresh() = shared.refresh()
    fun loadMore() = shared.loadMore()
    fun searchById(query: String) = shared.searchById(query)
}


