package org.example.project.myPool.domian.usecase

import org.example.project.myPool.domian.model.OrderInfo
import org.example.project.socket.Coordinates


// Platform-independent interface
expect class ComputeDistancesUseCase() {
    operator fun invoke(
        origin: Coordinates,
        orders: List<OrderInfo>,
    ): List<OrderInfo>
}
