package org.example.project.socket


import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile
import io.ktor.client.request.url
import io.ktor.websocket.readText
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import org.example.project.SecureTokenStore
import kotlin.coroutines.CoroutineContext

private const val WS_CLOSE_NORMAL = 1000

internal class ConnectionController(
    private val baseWsUrl: String,
    private val client: HttpClient,
    private val tokenStore: SecureTokenStore,
    private val listener: ConnectionListener,
    private val logTag: String = "LMD-WS",
    coroutineContext: CoroutineContext = Dispatchers.Default
) {
    private val scope = CoroutineScope(coroutineContext + Job())

    @Volatile private var subscribedTopic: String? = null
    @Volatile private var readJob: Job? = null

    suspend fun connect(channelName: String) {
        val access = tokenStore.getAccessToken().orEmpty()
        val topic = "realtime:public:$channelName"

        readJob?.cancel()
        readJob = scope.launch {
            try {
                val wsUrl = "$baseWsUrl&access_token=$access"
                logD(logTag, "Connecting to $wsUrl")

                client.webSocket(request = { url(wsUrl) }) {
                    listener.onOpen()

                    val join = """{"topic":"$topic","event":"phx_join","payload":{},"ref":"1"}"""
                    send(Frame.Text(join))
                    logD(logTag, "JOIN -> $join")

                    if (subscribedTopic != topic) {
                        val subscribe = """
                            {"topic":"$topic","event":"postgres_changes",
                             "payload":{"event":"*","schema":"public","table":"orders"},"ref":"2"}
                        """.trimIndent()
                        send(Frame.Text(subscribe))
                        logD(logTag, "SUB -> $subscribe")
                        subscribedTopic = topic
                    } else {
                        logD(logTag, "SUB skipped (already subscribed to $topic)")
                    }

                    for (frame in incoming) {
                        if (!isActive) break
                        when (frame) {
                            is Frame.Text -> listener.onMessage(frame.readText())
                            is Frame.Close -> listener.onClosed(WS_CLOSE_NORMAL, "Closed")
                            else -> Unit
                        }
                    }
                }
            } catch (t: Throwable) {
                listener.onFailure(null, t.message, t)
            }
        }
    }

    suspend fun close() {
        val job = readJob ?: return
        job.cancel()
        job.cancelAndJoin()
    }
}
internal interface ConnectionListener {
    fun onOpen()
    fun onClosed(code: Int, reason: String)
    fun onFailure(httpCode: Int? = null, message: String? = null, t: Throwable? = null)
    fun onMessage(text: String)
}

private inline fun logD(tag: String, msg: String) = println("D/$tag: $msg")
