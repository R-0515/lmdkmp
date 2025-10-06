package org.example.project.myPool.domian.usecase

import org.example.project.myPool.domian.model.OrderInfo

actual class ComputeDistancesUseCase actual constructor() {
    actual operator fun invoke(
        origin: Coordinates,
        orders: List<OrderInfo>
    ): List<OrderInfo> {
        TODO("Not yet implemented")
    }
}