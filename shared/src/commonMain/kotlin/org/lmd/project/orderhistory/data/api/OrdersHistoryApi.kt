package org.lmd.project.orderhistory.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import org.lmd.project.BuildKonfig
import org.lmd.project.SecureTokenStore
import org.lmd.project.delivery.data.api.ensureSuccess
import org.lmd.project.delivery.data.api.baseHttpClient
import org.lmd.project.orderhistory.data.dto.OrdersListResponse
import org.lmd.project.orderhistory.domain.model.OrderStatusCode

interface OrdersHistoryApi {
    suspend fun getOrders(
        page: Int = 1,
        limit: Int = 20,
        statusIds: String = OrderStatusCode.fromList(
            listOf(
                OrderStatusCode.CANCELLED,
                OrderStatusCode.FAILED,
                OrderStatusCode.DONE,
            )
        )
    ): OrdersListResponse
}

class OrdersHistoryApiKtor(
    private val client: HttpClient = baseHttpClient(),
    private val baseUrl: String = BuildKonfig.BASE_URL,
    private val tokenStore: SecureTokenStore,
) : OrdersHistoryApi {

    override suspend fun getOrders(
        page: Int,
        limit: Int,
        statusIds: String,
    ): OrdersListResponse {
        val accessToken = tokenStore.getAccessToken() ?: ""

        val response: HttpResponse = client.get("${baseUrl}orders-list") {
            url {
                parameters.append("status_id", statusIds)
                parameters.append("page", page.toString())
                parameters.append("limit", limit.toString())
            }
            headers {
                append("apikey", BuildKonfig.SUPABASE_KEY)
                append("Authorization", "Bearer $accessToken")
            }
            accept(ContentType.Application.Json)
        }

        val text = response.bodyAsText()
        println("🧾 OrdersHistoryApiKtor: $text")
        ensureSuccess(response.status, text)
        return response.body()
    }
}
