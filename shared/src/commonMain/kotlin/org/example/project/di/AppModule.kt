package org.example.project.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import org.example.project.location.data.repository.LocationRepositoryImpl
import org.example.project.location.domain.repository.LocationRepository
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase
import org.koin.dsl.module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.example.project.socket.SocketIntegration
import org.example.project.BuildKonfig
import org.example.project.delivery.data.api.DeliveriesLogApi
import org.example.project.delivery.data.api.DeliveriesLogApiKtor
import org.example.project.delivery.data.repositoryimpl.DeliveriesLogRepositoryImpl
import org.example.project.delivery.domain.repository.DeliveriesLogRepository
import org.example.project.delivery.domain.usecase.GetDeliveriesLogPageUseCase
import org.example.project.orderhistory.data.api.OrdersHistoryApi
import org.example.project.orderhistory.data.api.OrdersHistoryApiKtor
import org.example.project.orderhistory.data.repositoryimpl.OrdersRepositoryImpl
import org.example.project.orderhistory.domain.repository.OrdersRepository
import org.example.project.orderhistory.domain.usecase.GetOrdersUseCase


val locationCommonModule = module {
    // Repository API (depends on LocationProvider)
    single<LocationRepository> { LocationRepositoryImpl(get()) }

    // UseCase (depends on Repository)
    factory { GetDeviceLocationsUseCase(get()) }
}

val networkModule = module {
    single {
        HttpClient {
            install(WebSockets)
        }
    }
}

val socketModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single {
        SocketIntegration(
            baseWsUrl =  BuildKonfig.WS_BASE_URL,
            client = get(),
            tokenStore = get()
        )
    }
}

val deliveryModule = module {
    single<DeliveriesLogApi> { DeliveriesLogApiKtor(tokenStore = get()) }
    single<DeliveriesLogRepository> { DeliveriesLogRepositoryImpl(get()) }
    factory { GetDeliveriesLogPageUseCase(get()) }
}
val orderHistoryModule = module {
    single<OrdersHistoryApi> { OrdersHistoryApiKtor(tokenStore = get()) }
    single<OrdersRepository> { OrdersRepositoryImpl(get()) }
    factory { GetOrdersUseCase(get()) }
}
