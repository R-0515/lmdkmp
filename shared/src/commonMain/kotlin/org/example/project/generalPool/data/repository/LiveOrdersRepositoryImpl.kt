package org.example.project.generalPool.data.repository

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import org.example.project.generalPool.data.datasource.remote.LiveOrdersApiService
import org.example.project.generalPool.domain.model.PageInfo
import org.example.project.generalPool.domain.model.PagedOrdersResponse
import org.example.project.generalPool.domain.repository.LiveOrdersRepository
import org.example.project.socket.Order
import org.example.project.socket.SocketIntegration
import kotlin.coroutines.cancellation.CancellationException

class LiveOrdersRepositoryImpl(
    private val liveOrdersApi: LiveOrdersApiService,
    private val socket: SocketIntegration,
) : LiveOrdersRepository {

    // fetch all live orders with pagination support
    override suspend fun getAllLiveOrders(pageSize: Int): Result<List<Order>> =
        try {
            val all = mutableListOf<Order>()
            var page = 1

            while (true) {
                val resp = fetchPage(liveOrdersApi, page, pageSize)

                val (items, pi) = extractOrdersAndPageInfo(resp.data)
                all += items

                val (currentPage, totalPages, hintedNext) = resolvePageMeta(resp, page, pi)
                val next = computeNextPage(currentPage, totalPages, hintedNext)

                if (items.isEmpty() || next == null) break
                page = next
            }

            Result.success(all)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            // Keep it simple and multiplatform-safe
            Result.failure(IllegalStateException(t.message ?: "Failed to load orders", t))
        }

    // Connect to socket channel and start listening for live order updates
    override fun connectToOrders(channelName: String) {
        socket.connect(channelName)
        socket.startChannelListener()
    }

    // disconnect socket connection
    override fun disconnectFromOrders() {
        socket.disconnect()
    }

    // retry socket connection after failure
    override fun retryConnection() {
        socket.retryConnection()
    }

    // send request to update order status through socket
    override fun updateOrderStatus(
        orderId: String,
        status: String,
    ) {
        socket.updateOrderStatus(orderId, status)
    }

    // expose live orders from socket as a flow
    override fun orders() = socket.orders

}

private val PREFERRED_ARRAY_KEYS =
    listOf(
        "items",
        "orders",
        "initial_orders",
        "results",
        "rows",
        "list",
        "liveOrders",
        "data",
    )

// call API endpoint to fetch a single page of orders
private suspend fun fetchPage(
    api: LiveOrdersApiService,
    page: Int,
    limit: Int,
): PagedOrdersResponse {
    val resp = api.getLiveOrdersPage(page = page, limit = limit, search = null)
    if (!resp.success) {
        throw IllegalStateException(resp.message ?: "Failed to load orders")
    }
    return resp
}

// resolve current page, total pages, and hinted text page
private fun resolvePageMeta(
    resp: PagedOrdersResponse,
    fallbackPage: Int,
    pi: PageInfo,
): Triple<Int, Int?, Int?> {
    val currentPage = resp.page ?: pi.page ?: fallbackPage
    val totalPages = resp.totalPages ?: pi.totalPages
    val nextPage = resp.nextPage ?: pi.nextPage
    return Triple(currentPage, totalPages, nextPage)
}

// decide the next page number to fetch
private fun computeNextPage(
    currentPage: Int,
    totalPages: Int?,
    hintedNextPage: Int?,
): Int? =
    hintedNextPage
        ?: if (totalPages != null && currentPage < totalPages) currentPage + 1 else null

// extract orders and page info from JSON
private fun extractOrdersAndPageInfo(data: JsonElement?): Pair<List<Order>, PageInfo> {
    if (data == null || data is JsonNull) return emptyList<Order>() to PageInfo()

    return when {
        data is JsonArray -> {
            val items: List<Order> = decodeOrdersArray(data)
            items to PageInfo()
        }

        data is JsonObject -> {
            val (arr, usedKey) = data.findFirstItemsArray()
            val items: List<Order> = if (arr != null) decodeOrdersArray(arr) else emptyList()
            val pageInfo = data.extractPageInfo()
            items to pageInfo
        }

        else -> emptyList<Order>() to PageInfo()
    }
}

private fun decodeOrdersArray(arr: JsonArray): List<Order> =
    runCatching { Json.decodeFromJsonElement<List<Order>>(arr) }
        .getOrElse { emptyList() }


// search JSON object for an array of orders
private fun JsonObject.findFirstItemsArray(): Pair<JsonArray?, String?> {
    var foundArray: JsonArray? = null
    var usedKey: String? = null

    for (k in PREFERRED_ARRAY_KEYS) {
        val el = this[k]
        if (el is JsonArray) {
            foundArray = el
            usedKey = k
            break
        }
    }

    if (foundArray == null) {
        for ((k, v) in this) {
            if (v is JsonArray) {
                foundArray = v
                usedKey = k
                break
            }
        }
    }

    return foundArray to usedKey
}

// extract pagination info
private fun JsonObject.extractPageInfo(): PageInfo {
    val pageInfoObj =
        this["pageInfo"]?.jsonObjectOrNull()
            ?: this["pagination"]?.jsonObjectOrNull()

    return PageInfo(
        page = firstFrom(pageInfoObj, "page", "current_page") { k -> optInt(k) },
        totalPages = firstFrom(pageInfoObj, "totalPages", "total_pages") { k -> optInt(k) },
        nextPage = firstFrom(pageInfoObj, "nextPage") { k -> optInt(k) },
        hasMore = firstFrom(
            pageInfoObj,
            "hasMore",
            "hasNextPage",
            "has_next_page"
        ) { k -> optBool(k) },
        cursor = firstFrom(pageInfoObj, "cursor", "endCursor") { k -> optString(k) },
        nextCursor = firstFrom(pageInfoObj, "nextCursor", "endCursor") { k -> optString(k) },
    )
}

private inline fun <T> JsonObject.firstFrom(
    other: JsonObject?,
    vararg keys: String,
    crossinline getter: JsonObject.(String) -> T?,
): T? {
    var found: T? = null
    keys.forEach { k ->
        if (found == null) {
            found = this.getter(k) ?: other?.getter(k)
        }
    }
    return found
}

// access extensions

private fun JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject

private fun JsonObject.optString(key: String): String? {
    val el = this[key] ?: return null
    val prim = el as? JsonPrimitive ?: return null
    return if (prim.isString) prim.content else null
}

private fun JsonObject.optInt(key: String): Int? {
    val el = this[key] ?: return null
    val prim = el as? JsonPrimitive ?: return null
    return prim.intOrNull ?: prim.content.toIntOrNull()
}

private fun JsonObject.optBool(key: String): Boolean? {
    val el = this[key] ?: return null
    val prim = el as? JsonPrimitive ?: return null
    return prim.booleanOrNull ?: when (prim.content.lowercase()) {
        "true" -> true
        "false" -> false
        else -> null
    }
}