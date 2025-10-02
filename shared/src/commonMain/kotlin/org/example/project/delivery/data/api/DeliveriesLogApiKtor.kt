package org.example.project.delivery.data.api

import org.example.project.delivery.data.dto.OrdersEnvelope

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
//    private val client: HttpClient = baseHttpClient(),
//    private val baseUrl: String = BuildKonfig.BASE_URL,
) : DeliveriesLogApi {
    //
//    override suspend fun getOrders(
//        page: Int,
//        limit: Int,
//        statusIds: String?,
//        search: String?,
//        assignedAgentId: String?,
//        userOrdersOnly: Boolean?,
//    ): OrdersEnvelope {
//        val url = URLBuilder(baseUrl).apply {
//            // If your BASE_URL already ends with '/', Ktor handles it.
//            path("orders-list")
//            parameters.apply {
//                append("page", page.toString())
//                append("limit", limit.toString())
//                statusIds?.let { append("status_ids", it) }
//                search?.let { append("search", it) }
//                assignedAgentId?.let { append("assigned_agent_id", it) }
//                userOrdersOnly?.let { append("user_orders_only", it.toString()) }
//            }
//        }.buildString()
//
//        val resp: HttpResponse = client.get(url) {
//            accept(ContentType.Application.Json)
//        }
//
//        val text = resp.bodyAsText()
//        ensureSuccess(resp.status, text)
//        return resp.body()
//    }
    override suspend fun getOrders(
        page: Int,
        limit: Int,
        statusIds: String?,
        search: String?,
        assignedAgentId: String?,
        userOrdersOnly: Boolean?
    ): OrdersEnvelope {
        TODO("Not yet implemented")
    }
}