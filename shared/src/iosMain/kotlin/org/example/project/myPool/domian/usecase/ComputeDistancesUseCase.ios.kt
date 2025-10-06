package org.example.project.myPool.domian.usecase

import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.socket.Coordinates

actual class ComputeDistancesUseCase actual constructor() {
    actual operator fun invoke(
        origin: Coordinates,
        orders: List<OrderInfo>
    ): List<OrderInfo> {
        TODO("Not yet implemented")
    }
}