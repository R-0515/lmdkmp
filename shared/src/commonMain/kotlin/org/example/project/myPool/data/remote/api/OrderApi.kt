package org.example.project.myPool.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import org.example.project.BuildKonfig
import org.example.project.SecureTokenStore
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import org.example.project.myPool.data.remote.dto.OrdersEnvelope

class OrdersApi(
    private val client: HttpClient = baseHttpClient(),
    private val baseUrl: String = BuildKonfig.BASE_URL,
    private val tokenStore: SecureTokenStore
) {
    suspend fun getOrders(
        page: Int,
        limit: Int,
        statusIds: String? = null,
        search: String? = null,
        assignedAgentId: String? = null,
        userOrdersOnly: Boolean? = null,
    ): OrdersEnvelope {
        val accessToken = tokenStore.getAccessToken().orEmpty()

        val resp: HttpResponse = client.get("${baseUrl}orders-list") {
            url {
                parameters.append("page", page.toString())
                parameters.append("limit", limit.toString())
                statusIds?.let { parameters.append("status_ids", it) }
                search?.let { parameters.append("search", it) }
                assignedAgentId?.let { parameters.append("assigned_agent_id", it) }
                userOrdersOnly?.let { parameters.append("user_orders_only", it.toString()) }
            }
            accept(ContentType.Application.Json)
            headers {
                append("apikey", BuildKonfig.SUPABASE_KEY)
                append("Authorization", "Bearer $accessToken")
            }
        }

        println("🧾 Access token: $accessToken")
        val text = resp.bodyAsText()
        println("🚀 OrdersApiKtor response: $text")

        ensureSuccess(resp.status, text)

        return resp.body()
    }

    private fun ensureSuccess(status: HttpStatusCode, body: String) {
        if (!status.isSuccess()) {
            throw Exception("Request failed (${status.value}): $body")
        }
    }
}