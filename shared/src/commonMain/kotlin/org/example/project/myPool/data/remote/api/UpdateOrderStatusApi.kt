package org.example.project.myPool.data.remote.api

import org.example.project.myPool.data.remote.dto.UpdateOrderStatusEnvelope
import org.example.project.myPool.data.remote.dto.UpdateOrderStatusRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import org.example.project.BuildKonfig
import org.example.project.SecureTokenStore

class UpdateOrderStatusApi(
    private val client: HttpClient = baseHttpClient(),
    private val baseUrl: String = BuildKonfig.BASE_URL,
    private val tokenStore: SecureTokenStore
) {

    suspend fun updateOrderStatus(
        body: UpdateOrderStatusRequest,
    ): UpdateOrderStatusEnvelope {
        val accessToken = tokenStore.getAccessToken() ?: ""

        val resp: HttpResponse = client.post("${baseUrl}update-order-status") {
            contentType(ContentType.Application.Json)
            setBody(body)
            headers {
                append("apikey", BuildKonfig.SUPABASE_KEY)
                append("Authorization", "Bearer $accessToken")
            }
        }

        val text = resp.bodyAsText()
        println("🚀 UpdateOrderStatusApiKtor response: $text")
        ensureSuccess(resp.status, text)
        return resp.body()
    }
}
