package org.lmd.project.delivery.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import org.lmd.project.delivery.data.dto.OrdersEnvelope
import org.lmd.project.BuildKonfig
import org.lmd.project.SecureTokenStore

interface DeliveriesLogApi {
    suspend fun getOrders(
        page: Int,
        limit: Int,
        statusIds: String? = null,
        search: String? = null,
        assignedAgentId: String? = null,
        userOrdersOnly: Boolean? = null,
    ): OrdersEnvelope
}
class DeliveriesLogApiKtor(
    private val client: HttpClient = baseHttpClient(),
    private val baseUrl: String = BuildKonfig.BASE_URL,
    private val tokenStore: SecureTokenStore
) : DeliveriesLogApi {

    override suspend fun getOrders(
        page: Int,
        limit: Int,
        statusIds: String?,
        search: String?,
        assignedAgentId: String?,
        userOrdersOnly: Boolean?,
    ): OrdersEnvelope {
        val accessToken = tokenStore.getAccessToken() ?: ""

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

        val text = resp.bodyAsText()
        println("🚀 DeliveriesLogApiKtor response: $text")
        ensureSuccess(resp.status, text)
        return resp.body()
    }
}