package org.example.project.myPool.ui.viewmodel

import android.content.Context
import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.myPool.ui.logic.OrdersListController
import org.example.project.myPool.ui.logic.OrdersListControllerDeps
import org.example.project.myPool.ui.logic.OrdersListPublisher
import org.example.project.myPool.ui.logic.OrdersPager
import org.example.project.myPool.ui.logic.OrdersStore
import org.example.project.myPool.ui.logic.OrdersThrottle
import org.example.project.util.toUserMessage
import java.io.IOException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException


/**
 * Android-specific wrapper with Context and full error handling
 */
class OrdersListControllerAndroid(
    private val deps: OrdersListControllerDepsAndroid,
    private val scope: CoroutineScope,
) {
    private val base = OrdersListController(
        deps = deps.toSharedDeps(),
        scope = scope,
        onError = { message, retry -> deps.errors.postError(message, retry) },
    )

    fun setCurrentUserId(id: String?) = base.setCurrentUserId(id)

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadInitial(context: Context) {
        val alreadyHasData = deps.store.state.value.orders.isNotEmpty()
        if (deps.store.state.value.isLoading) return

        deps.store.state.update { it.copy(isLoading = !alreadyHasData, errorMessage = null) }

        scope.launch {
            try {
                val (items, endReached) = deps.pager.getPage(
                    page = 1,
                    bypassCache = true,
                    assignedAgentId = deps.store.currentUserId.value,
                ) to false
                deps.publisher.publishFirstPage(items, endReached)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: SocketTimeoutException) {
                deps.errors.handleInitialError(e, alreadyHasData, context) { ctx -> loadInitial(ctx) }
            } catch (e: IOException) {
                deps.errors.handleInitialError(e, alreadyHasData, context) { ctx -> loadInitial(ctx) }
            } catch (e: HttpException) {
                deps.errors.postError(e.toUserMessage()) { loadInitial(context) }
            } catch (e: SSLHandshakeException) {
                deps.errors.postError(e.toUserMessage()) { loadInitial(context) }
            } catch (e: UnknownHostException) {
                deps.errors.postError(e.toUserMessage()) { loadInitial(context) }
            } finally {
                deps.store.state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refresh(context: Context) = base.refreshStrict()
    fun loadNextPage(context: Context) = base.loadNextPage()
}

data class OrdersListControllerDepsAndroid(
    val store: OrdersStore,
    val pager: OrdersPager,
    val publisher: OrdersListPublisher,
    val errors: OrdersListErrorHandler,
    val throttle: OrdersThrottle,
) {
    fun toSharedDeps(): OrdersListControllerDeps = OrdersListControllerDeps(
        store = store,
        pager = pager,
        publisher = publisher,
        throttle = throttle,
    )
}