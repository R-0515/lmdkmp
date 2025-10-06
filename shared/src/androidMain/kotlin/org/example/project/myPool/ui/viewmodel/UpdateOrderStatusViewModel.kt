package org.example.project.myPool.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.UserStore
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.domian.model.toApiId
import org.example.project.myPool.domian.usecase.UpdateOrderStatusUseCase
import org.example.project.myPool.ui.logic.UpdateOrderStatusLogic
import org.example.project.util.toUserMessage
import kotlin.coroutines.cancellation.CancellationException

class UpdateOrderStatusViewModel(
    updateStatus: UpdateOrderStatusUseCase,
    userStore: UserStore,
) : ViewModel() {
    private val logic = UpdateOrderStatusLogic(
        updateStatus = updateStatus,
        userStore = userStore,
        scope = viewModelScope,
    )

    val updatingIds = logic.updatingIds
    val success = logic.success
    val error = logic.error
    val currentUserId = logic.currentUserId

    fun update(orderId: String, targetStatus: OrderStatus, assignedAgentId: String? = null) =
        logic.update(orderId, targetStatus, assignedAgentId)
}