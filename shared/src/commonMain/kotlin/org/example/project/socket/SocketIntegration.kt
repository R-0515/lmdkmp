package org.example.project.socket

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlin.concurrent.Volatile
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.example.project.SecureTokenStore

private const val WS_CLOSE_NORMAL = 1000
private const val HTTP_UNAUTHORIZED = 401
private const val DEFAULT_RETRY_DELAY_MS = 3000L
private const val HEARTBEAT_INTERVAL_MS = 28_000L
class SocketIntegration(
    private val baseWsUrl: String,
    private val client: HttpClient,
    private val tokenStore: SecureTokenStore
) {
    private val logTag = "LMD-WS"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<SocketEvent> = _events

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var hbJob: Job? = null
    @Volatile
    private var refCounter = 1

    private val orderStore = OrderStore()
    fun getOrderChannel(): Channel<Order> = orderStore.getChannel()
    val orders: StateFlow<List<Order>> = orderStore.state

    private val recon = ReconnectController(scope) { currentChannelName?.let { connect(it) } }

    private val router = MessageRouter(orderStore, _events, logTag)

    private var session: DefaultClientWebSocketSession? = null
    private var lastAccess: String? = null
    private var currentChannelName: String? = null
    private var isDestroyed = false
    @Volatile private var listenerStarted = false

     fun connect(channelName: String) {
        if (isDestroyed) {
            logW(logTag, "connect() called after destroy")
            return
        }
        val state = _connectionState.value
        val alreadyConnected =
            currentChannelName == channelName &&
                    (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING)

        when {
            alreadyConnected -> logD(logTag, "connect() ignored: already $state to $channelName")
            tokenStore.getAccessToken().isNullOrEmpty() ->
                _connectionState.value = ConnectionState.ERROR("No authentication token")
            else -> {
                closeSession(reason = "Reconnecting to $channelName")
                lastAccess = tokenStore.getAccessToken()
                currentChannelName = channelName
                _connectionState.value = ConnectionState.CONNECTING
                recon.cancel()
                scope.launch { openWebSocket(channelName) }
            }
        }
    }

    fun disconnect() {
        recon.cancel()
        closeSession(reason = "User disconnected")
        currentChannelName = null
        listenerStarted = false
        _connectionState.value = ConnectionState.DISCONNECTED
    }

     fun retryConnection() {
        if (isDestroyed) {
            logW(logTag, "retryConnection() after destroy")
            return
        }
        recon.cancel()
        currentChannelName?.let { connect(it) }
    }

    fun startChannelListener() {
        if (listenerStarted) return
        listenerStarted = true
        scope.launch {
            for (order in orderStore.getChannel()) {
                logD(logTag, "ORDER FROM CHANNEL: #${order.orderNumber} - ${order.customerName}")
            }
        }
    }

     fun updateOrderStatus(orderId: String, status: String) {
        val access = tokenStore.getAccessToken()
        if (access.isNullOrEmpty()) return
        val payload = """{"type":"UPDATE","payload":{"order_id":"$orderId","status":"$status"}}"""
        scope.launch { session?.send(Frame.Text(payload)) }
    }

     fun reconnectIfTokenChanged(currentAccess: String?) {
        if (!currentAccess.isNullOrBlank() && currentAccess != lastAccess) {
            disconnect()
            connect(currentChannelName ?: "orders")
        }
    }


    private suspend fun openWebSocket(channelName: String) {
        val access = tokenStore.getAccessToken().orEmpty()
        val wsUrl = "$baseWsUrl&access_token=$access"
        val topic = "realtime:public:$channelName"

        try {
            client.webSocket(request = { url(wsUrl) }) {
                session = this
                _events.tryEmit(SocketEvent.Open)
                _connectionState.value = ConnectionState.CONNECTED
                startHeartbeat()

                // JOIN
                val join = """{"topic":"$topic","event":"phx_join","payload":{},"ref":"1"}"""
                send(Frame.Text(join))
                logD(logTag, "JOIN -> $join")

                // SUBSCRIBE
                val subscribe = """
                    {"topic":"$topic","event":"postgres_changes",
                     "payload":{"event":"*","schema":"public","table":"orders"},"ref":"2"}
                """.trimIndent()
                send(Frame.Text(subscribe))
                logD(logTag, "SUB -> $subscribe")

                // Read loop
                for (frame in incoming) {
                    if (!isActive) break
                    when (frame) {
                        is Frame.Text -> router.route(frame.readText())
                        is Frame.Close -> {
                            stopHeartbeat()
                            _events.tryEmit(SocketEvent.Closed(WS_CLOSE_NORMAL, "Closed"))
                            _connectionState.value = ConnectionState.DISCONNECTED
                            recon.schedule(DEFAULT_RETRY_DELAY_MS)
                        }
                        else -> Unit
                    }
                }
            }
        } catch (t: Throwable) {
            stopHeartbeat()
            _events.tryEmit(SocketEvent.Error(t))
            _connectionState.value = ConnectionState.ERROR(t.message ?: "Connection failed")
            recon.schedule(DEFAULT_RETRY_DELAY_MS)
        } finally {
            session = null
        }
    }

    private fun startHeartbeat() {
        hbJob?.cancel()
        hbJob = scope.launch {
            while (isActive) {
                val beat =
                    """{"topic":"phoenix","event":"heartbeat","payload":{},"ref":"${refCounter++}"}"""
                session?.send(Frame.Text(beat))
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
    }

    private fun stopHeartbeat() {
        hbJob?.cancel()
        hbJob = null
    }

    private fun closeSession(reason: String) {
        stopHeartbeat()
        session = null
        logD(logTag, "Session closed: $reason")
    }
}


sealed class ConnectionState {
    data object CONNECTING : ConnectionState()
    data object CONNECTED : ConnectionState()
    data object DISCONNECTED : ConnectionState()
    data class ERROR(val message: String) : ConnectionState()
}

private inline fun logD(tag: String, msg: String) = println("D/$tag: $msg")
private inline fun logW(tag: String, msg: String) = println("W/$tag: $msg")
