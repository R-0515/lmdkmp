package org.example.project.myPool.ui.viewmodel

import android.content.Context
import android.location.Location
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.myPool.domian.usecase.ComputeDistancesUseCase
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus
import org.example.project.myPool.ui.logic.OrdersListHelpersCommon
import org.example.project.myPool.ui.logic.OrdersStore
import org.example.project.myPool.ui.model.LocalUiOnlyStatusBus
import org.example.project.myPool.ui.state.MyOrdersUiState
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.min

/**
 * Android-specific extension functions that interact with Context and the LocalUiOnlyStatusBus.
 */
class OrdersListHelpersAndroid(
    val helpers: OrdersListHelpersCommon,
) {

    fun handlePagingError(
        msg: String,
        context: Context,
        state: MutableStateFlow<MyOrdersUiState>,
        retry: (Context) -> Unit,
    ) {
        LocalUiOnlyStatusBus.errorEvents.tryEmit(msg to { retry(context) })
        state.update { it.copy(isLoadingMore = false) }
    }

    fun handleInitialLoadError(
        e: Exception,
        alreadyHasData: Boolean,
        context: Context,
        state: MutableStateFlow<MyOrdersUiState>,
        retry: (Context) -> Unit,
    ) {
        val msg = messageFor(e)
        state.update {
            it.copy(
                isLoading = false,
                errorMessage = if (!alreadyHasData) msg else null,
            )
        }
        if (alreadyHasData) {
            LocalUiOnlyStatusBus.errorEvents.tryEmit(msg to { retry(context) })
        }
    }

    fun messageFor(e: Exception): String =
        when (e) {
            is ClientRequestException -> "Client error: ${e.response.status.value} ${e.response.status.description}"
            is ServerResponseException -> "Server error: ${e.response.status.value} ${e.response.status.description}"
            is RedirectResponseException -> "Redirection: ${e.response.status.value}"
            is HttpRequestTimeoutException -> "Request timed out"
            is UnknownHostException -> "No internet connection"
            is SocketTimeoutException -> "Request timed out"
            is IOException -> "Network error"
            else -> e.message ?: "Unknown error"
        }
}