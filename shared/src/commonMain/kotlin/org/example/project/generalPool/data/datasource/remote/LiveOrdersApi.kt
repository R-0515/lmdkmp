package org.example.project.generalPool.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.example.project.generalPool.domain.model.PagedOrdersResponse

interface LiveOrdersApiService {
    suspend fun getLiveOrdersPage(
        page: Int = 1,
        limit: Int = 50,
        search: String? = null
    ): PagedOrdersResponse
}

class LiveOrdersApiServiceImpl(
    private val client: HttpClient,
    private val baseUrl: String,
) : LiveOrdersApiService {
    override suspend fun getLiveOrdersPage(
        page: Int,
        limit: Int,
        search: String?
    ): PagedOrdersResponse =
        client.get("$baseUrl/live-orders") {
            parameter("page", page)
            parameter("limit", limit)
            if (!search.isNullOrBlank()) parameter("search", search)
        }.body()
}
