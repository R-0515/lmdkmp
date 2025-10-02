package org.example.project.generalPool.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.example.project.generalPool.domain.model.PagedOrdersResponse

class LiveOrdersApiService(private val client: HttpClient, private val baseUrl: String) {
    suspend fun getLiveOrdersPage(
        page: Int = 1,
        limit: Int = 50,
        search: String? = null
    ): PagedOrdersResponse {
        return client.get("$baseUrl/live-orders") {
            parameter("page", page)
            parameter("limit", limit)
            if (!search.isNullOrBlank()) parameter("search", search)
        }.body()
    }
}