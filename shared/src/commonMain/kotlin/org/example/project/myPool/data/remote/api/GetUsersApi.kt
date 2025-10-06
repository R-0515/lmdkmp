package org.example.project.myPool.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import org.example.project.BuildKonfig
import org.example.project.SecureTokenStore
import org.example.project.myPool.domian.model.ActiveUsersEnvelope

class GetUsersApi(
    private val client: HttpClient = baseHttpClient(),
    private val baseUrl: String = BuildKonfig.BASE_URL,
    private val tokenStore: SecureTokenStore
) {

    suspend fun getActiveUsers(): ActiveUsersEnvelope {
        val accessToken = tokenStore.getAccessToken() ?: ""

        val resp: HttpResponse = client.get("${baseUrl}get-all-users") {
            accept(ContentType.Application.Json)
            headers {
                append("apikey", BuildKonfig.SUPABASE_KEY)
                append("Authorization", "Bearer $accessToken")
            }
        }

        val text = resp.bodyAsText()
        println("🚀 GetUsersApiKtor response: $text")
        ensureSuccess(resp.status, text)
        return resp.body()
    }
}