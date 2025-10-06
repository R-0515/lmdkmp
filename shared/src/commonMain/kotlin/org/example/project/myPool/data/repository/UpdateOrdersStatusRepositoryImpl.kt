package org.example.project.myPool.data.repository

import org.example.project.myPool.data.remote.api.UpdateOrderStatusApi
import org.example.project.myPool.data.remote.dto.UpdateOrderStatusRequest
import org.example.project.myPool.data.remote.dto.UpdatedOrderData
import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.myPool.domian.model.OrderStatus.Companion.fromId
import org.example.project.myPool.domian.model.RelativeTime
import org.example.project.myPool.domian.repository.UpdateOrdersStatusRepository


class UpdateOrdersStatusRepositoryImpl(
    private val api: UpdateOrderStatusApi,
) : UpdateOrdersStatusRepository {

    override suspend fun updateOrderStatus(
        orderId: String,
        statusId: Int,
        assignedAgentId: String?,
    ): OrderInfo {
        val req = buildRequest(orderId, statusId, assignedAgentId)

        // 🚨 Removed android.util.Log (Android-only)
        println("OrderAction → POST body = $req")

        val env = api.updateOrderStatus(req)
        require(env.success) { env.message ?: "Failed to update order status" }

        return mapToOrderInfo(env.data, orderId, assignedAgentId)
    }

    private fun buildRequest(
        orderId: String,
        statusId: Int,
        assignedAgentId: String?,
    ) = UpdateOrderStatusRequest(
        orderId = orderId,
        statusId = statusId,
        assignedAgentId = assignedAgentId,
    )

    private fun mapToOrderInfo(
        d: UpdatedOrderData?,
        fallbackOrderId: String,
        fallbackAssigned: String?,
    ): OrderInfo =
        OrderInfo(
            id = d?.orderId ?: fallbackOrderId,
            name = d?.customerName.orEmpty(),
            orderNumber = d?.orderNumber.orEmpty(),
            timeAgo = RelativeTime.JustNow,
            itemsCount = 0,
            distanceKm = 0.0,
            lat = 0.0,
            lng = 0.0,
            status = fromId(d?.statusId),
            price = "---",
            customerPhone = null,
            details = d?.address,
            customerId = null,
            assignedAgentId = d?.assignedAgentId ?: fallbackAssigned,
        )
}
