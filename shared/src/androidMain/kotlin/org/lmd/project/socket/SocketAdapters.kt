package org.lmd.project.socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

actual fun defaultOrdersStore(): OrdersStore = OrderStoreAdapter()
actual fun defaultRouterFactory(): RouterFactory =
    { store, events, tag -> RouterAdapter(store, events, tag) }
actual fun defaultReconnectFactory(): ReconnectFactory =
    { scope, onRetry -> ReconnectorAdapter(scope, onRetry) }

private class OrderStoreAdapter(
    private val impl: OrderStore = OrderStore()
) : OrdersStore {
    override fun getChannel(): Channel<Order> = impl.getChannel()
    override val state: StateFlow<List<Order>> = impl.state

    fun underlying(): OrderStore = impl
}

private class RouterAdapter(
    store: OrdersStore,
    events: MutableSharedFlow<SocketEvent>,
    tag: String
) : Router {
    private val backing = MessageRouter(
        when (store) {
            is OrderStoreAdapter -> store.underlying()
            else -> OrderStore()
        },
        events,
        tag
    )
    override fun route(text: String) = backing.route(text)
}

private class ReconnectorAdapter(
    scope: CoroutineScope,
    onRetry: () -> Unit
) : Reconnector {
    private val backing = ReconnectController(scope, onRetry)
    override fun cancel() = backing.cancel()
    override fun schedule(delayMs: Long) = backing.schedule(delayMs)
}
