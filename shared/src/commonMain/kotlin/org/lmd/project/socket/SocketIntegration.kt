package org.lmd.project.socket

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
import org.lmd.project.SecureTokenStore
import kotlin.concurrent.Volatile

class SocketIntegration(
    private val baseWsUrl: String,
    private val client: HttpClient,
    private val tokenStore: SecureTokenStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    ,
    private val config: SocketConfig = SocketConfig(),
    private val ordersStore: OrdersStore = defaultOrdersStore(),
    private val reconnectFactory: ReconnectFactory = defaultReconnectFactory(),
    private val routerFactory: RouterFactory = defaultRouterFactory(),
    private val logger: Logger = ConsoleLogger()
) {
    private val logTag = "LMD-WS"

    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<SocketEvent> = _events

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var hbJob: Job? = null
    @Volatile
    private var refCounter = 1

    fun getOrderChannel(): Channel<Order> = ordersStore.getChannel()
    val orders: StateFlow<List<Order>> = ordersStore.state

    private val recon: Reconnector = reconnectFactory(scope) {
        currentChannelName?.let { channel ->
            scope.launch(dispatcher) {
                connect(channel)
            }
        }
    }
    private val router: Router by lazy { routerFactory(ordersStore, _events, logTag) }

    private var session: DefaultClientWebSocketSession? = null
    private var lastAccess: String? = null
    private var currentChannelName: String? = null
    private var isDestroyed = false
    @Volatile private var listenerStarted = false

    suspend fun connect(channelName: String) {
        if (isDestroyed) {
            logger.w(logTag, "connect() called after destroy"); return
        }
        val state = _connectionState.value
        val alreadyConnected = currentChannelName == channelName &&
                (state == ConnectionState.CONNECTED || state == ConnectionState.CONNECTING)

        when {
            alreadyConnected ->
                logger.d(logTag, "connect() ignored: already $state to $channelName")

            tokenStore.getAccessToken().isNullOrEmpty() ->
                _connectionState.value = ConnectionState.ERROR("No authentication token")

            else -> {
                closeSession(reason = "Reconnecting to $channelName")
                lastAccess = tokenStore.getAccessToken()
                currentChannelName = channelName
                _connectionState.value = ConnectionState.CONNECTING
                recon.cancel()
                scope.launch(dispatcher) { openWebSocket(channelName) }
            }
        }
    }

    suspend fun disconnect() {
        recon.cancel()
        closeSession(reason = "User disconnected")
        currentChannelName = null
        listenerStarted = false
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    suspend fun retryConnection() {
        if (isDestroyed) { logger.w(logTag, "retryConnection() after destroy"); return }
        recon.cancel()
        currentChannelName?.let { connect(it) }
    }

    fun startChannelListener() {
        if (listenerStarted) return
        listenerStarted = true
        scope.launch(dispatcher) {
            for (order in ordersStore.getChannel()) {
                logger.d(logTag, "ORDER FROM CHANNEL: #${order.orderNumber} - ${order.customerName}")
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        val access = tokenStore.getAccessToken()
        if (access.isNullOrEmpty()) return
        val payload = """{"type":"UPDATE","payload":{"order_id":"$orderId","status":"$status"}}"""
        scope.launch(dispatcher) { session?.send(Frame.Text(payload)) }
    }

    suspend fun reconnectIfTokenChanged(currentAccess: String?) {
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

                val join = """{"topic":"$topic","event":"phx_join","payload":{},"ref":"1"}"""
                send(Frame.Text(join))
                logger.d(logTag, "JOIN -> $join")

                val subscribe = """
                    {"topic":"$topic","event":"postgres_changes",
                     "payload":{"event":"*","schema":"public","table":"orders"},"ref":"2"}
                """.trimIndent()
                send(Frame.Text(subscribe))
                logger.d(logTag, "SUB -> $subscribe")

                for (frame in incoming) {
                    if (!isActive) break
                    when (frame) {
                        is Frame.Text -> router.route(frame.readText())
                        is Frame.Close -> {
                            stopHeartbeat()
                            _events.tryEmit(SocketEvent.Closed(config.wsCloseNormal, "Closed"))
                            _connectionState.value = ConnectionState.DISCONNECTED
                            recon.schedule(config.defaultRetryDelayMs)
                        }
                        else -> Unit
                    }
                }
            }
        } catch (t: Throwable) {
            stopHeartbeat()
            _events.tryEmit(SocketEvent.Error(t))
            _connectionState.value = ConnectionState.ERROR(t.message ?: "Connection failed")
            recon.schedule(config.defaultRetryDelayMs)
        } finally {
            session = null
        }
    }

    private fun startHeartbeat() {
        hbJob?.cancel()
        hbJob = scope.launch(dispatcher) {
            while (isActive) {
                val beat = """{"topic":"phoenix","event":"heartbeat","payload":{},"ref":"${refCounter++}"}"""
                session?.send(Frame.Text(beat))
                delay(config.heartbeatIntervalMs)
            }
        }
    }

    private fun stopHeartbeat() {
        hbJob?.cancel()
        hbJob = null
    }

    private suspend fun closeSession(reason: String) {
        stopHeartbeat()
        session?.close()
        session = null
        logger.d(logTag, "Session closed: $reason")
    }
}

sealed class ConnectionState {
    data object CONNECTING : ConnectionState()
    data object CONNECTED : ConnectionState()
    data object DISCONNECTED : ConnectionState()
    data class ERROR(val message: String) : ConnectionState()
}
