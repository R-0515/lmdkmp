package org.example.project.generalPool.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import org.example.project.generalPool.domain.model.PagedOrdersResponse
import org.example.project.BuildKonfig
import org.example.project.BuildKonfig.BASE_URL
import org.example.project.SecureTokenStore

interface LiveOrdersApiService {
    suspend fun getLiveOrdersPage(
        page: Int = 1,
        limit: Int = 25,
        search: String? = null
    ): PagedOrdersResponse
}

class LiveOrdersApiKtor(
    tokenStore: SecureTokenStore,
    private val client: HttpClient = baseHttpClient(
        apikeyProvider = { BuildKonfig.SUPABASE_KEY },
        authProvider = {
            val anonKey = BuildKonfig.SUPABASE_KEY
            val userJwt = tokenStore.getAccessToken()
            "Bearer " + (userJwt?.takeIf { it.isNotBlank() } ?: anonKey)
        }
    ),
) : LiveOrdersApiService {

    override suspend fun getLiveOrdersPage(
        page: Int,
        limit: Int,
        search: String?
    ): PagedOrdersResponse {

        val response: HttpResponse = client.get("${BASE_URL}live-orders") {
            url {
                parameters.append("page", page.toString())
                parameters.append("limit", limit.toString())
                if (!search.isNullOrBlank()) parameters.append("search", search)
            }
        }
        return response.body()
    }
}
