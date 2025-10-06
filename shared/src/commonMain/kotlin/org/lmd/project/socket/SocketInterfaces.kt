package org.lmd.project.socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

interface OrdersStore {
    fun getChannel(): Channel<Order>
    val state: StateFlow<List<Order>>
}

interface Router {
    fun route(text: String)
}

interface Reconnector {
    fun cancel()
    fun schedule(delayMs: Long)
}

interface Logger {
    fun d(tag: String, msg: String)
    fun w(tag: String, msg: String)
}

data class SocketConfig(
    val wsCloseNormal: Int = 1000,
    val defaultRetryDelayMs: Long = 3_000L,
    val heartbeatIntervalMs: Long = 28_000L
)

typealias ReconnectFactory = (CoroutineScope, () -> Unit) -> Reconnector
typealias RouterFactory = (OrdersStore, MutableSharedFlow<SocketEvent>, String) -> Router
