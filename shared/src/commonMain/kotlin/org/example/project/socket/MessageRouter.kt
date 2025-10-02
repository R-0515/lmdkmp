package org.example.project.socket

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


internal class MessageRouter(
    private val store: OrderStore,
    private val events: MutableSharedFlow<SocketEvent>,
    private val logTag: String,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun route(text: String) {
        logD(logTag, "RAW -> $text")
        try {
            val root = json.parseToJsonElement(text).jsonObject
            val event = root["event"]?.jsonPrimitive?.contentOrNull.orEmpty()

            when (event) {
                "phx_reply" -> logD(logTag, "Channel joined")

                "INSERT", "UPDATE", "DELETE" -> handleClassic(root, event)
                "postgres_changes" -> handlePostgresChange(root)

                "presence_state", "presence_diff", "system", "ping", "phx_close" -> Unit
                else -> Unit
            }

            events.tryEmit(SocketEvent.Message(text))
        } catch (t: Throwable) {
            logE(logTag, "JSON parse error: ${t.message}. Raw=$text", t)
            events.tryEmit(SocketEvent.Error(t))
        }
    }

    private fun handleClassic(root: JsonObject, event: String) {
        val payload = root["payload"]?.asObjOrNull() ?: emptyObj()
        val record = payload["record"]?.asObjOrNull()
        val oldRecord = payload["old_record"]?.asObjOrNull()

        when (event) {
            "INSERT" -> {
                record?.let {
                    val order = json.decodeFromJsonElement<Order>(it)
                    logD(logTag, """INSERT -> #${order.orderNumber} • ${order.customerName}""")
                    store.add(order)
                }
            }

            "UPDATE" -> {
                record?.let {
                    val order = json.decodeFromJsonElement<Order>(it)
                    store.update(order)
                }
            }

            "DELETE" -> {
                val id = oldRecord?.get("id")?.jsonPrimitive?.contentOrNull
                if (id != null) store.remove(id) else logD(logTag, "DELETE -> no id")
            }
        }
    }

    private fun handlePostgresChange(root: JsonObject) {
        val payload = root["payload"]?.asObjOrNull()
        val data = payload?.get("data")?.asObjOrNull()
        val type = data?.get("eventType")?.jsonPrimitive?.contentOrNull
        when (type) {
            "INSERT" -> {
                val newJson = data?.get("new")?.asObjOrNull() ?: return
                val order = json.decodeFromJsonElement<Order>(newJson)
                logD(logTag, """INSERT -> #${order.orderNumber} • ${order.customerName}""")
                store.add(order)
            }

            "UPDATE" -> {
                val newJson = data?.get("new")?.asObjOrNull() ?: return
                val order = json.decodeFromJsonElement<Order>(newJson)
                store.update(order)
            }

            "DELETE" -> {
                val oldJson = data?.get("old")?.asObjOrNull()
                val id = oldJson?.get("id")?.jsonPrimitive?.contentOrNull
                if (id != null) store.remove(id)
            }
        }
    }
}

private fun JsonElement.asObjOrNull(): JsonObject? =
    (this as? JsonObject) ?: runCatching { this.jsonObject }.getOrNull()

private fun emptyObj() = JsonObject(emptyMap())

private inline fun logD(tag: String, msg: String) = println("D/$tag: $msg")
private inline fun logE(tag: String, msg: String, t: Throwable? = null) =
    println("E/$tag: $msg${t?.let { " • ${it.message}" } ?: ""}")

private val JsonElement?.contentOrNull: String?
    get() = this?.jsonPrimitive?.contentOrNull
