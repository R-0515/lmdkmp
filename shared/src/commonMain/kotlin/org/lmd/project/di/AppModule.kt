package org.lmd.project.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import org.lmd.project.location.data.repository.LocationRepositoryImpl
import org.lmd.project.location.domain.repository.LocationRepository
import org.lmd.project.location.domain.usecase.GetDeviceLocationsUseCase
import org.koin.dsl.module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.lmd.project.socket.SocketIntegration
import org.lmd.project.delivery.data.api.DeliveriesLogApi
import org.lmd.project.delivery.data.api.DeliveriesLogApiKtor
import org.lmd.project.delivery.data.repositoryimpl.DeliveriesLogRepositoryImpl
import org.lmd.project.delivery.domain.repository.DeliveriesLogRepository
import org.lmd.project.delivery.domain.usecase.GetDeliveriesLogPageUseCase
import org.lmd.project.generalPool.data.datasource.remote.LiveOrdersApiKtor
import org.lmd.project.generalPool.data.datasource.remote.LiveOrdersApiService
import org.lmd.project.generalPool.data.repository.LiveOrdersRepositoryImpl
import org.lmd.project.generalPool.domain.repository.LiveOrdersRepository
import org.lmd.project.generalPool.domain.usecase.LoadOrdersUseCase
import org.lmd.project.generalPool.domain.usecase.OrdersRealtimeUseCase
import org.lmd.project.location.domain.usecase.ComputeDistancesUseCase
import org.lmd.project.orderhistory.data.api.OrdersHistoryApi
import org.lmd.project.orderhistory.data.api.OrdersHistoryApiKtor
import org.lmd.project.orderhistory.data.repositoryimpl.OrdersRepositoryImpl
import org.lmd.project.orderhistory.domain.repository.OrdersRepository
import org.lmd.project.orderhistory.domain.usecase.GetOrdersUseCase


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
    single(named("wsDispatcher")) { Dispatchers.IO }
    single(named("wsScope")) { CoroutineScope(SupervisorJob()) }

    single {
        SocketIntegration(
            baseWsUrl = get(named("wsBaseUrl")),
            client = get(),
            tokenStore = get(),
            scope = get(named("wsScope")),
            dispatcher = get(named("wsDispatcher")),
            config = get(),
            logger = get()
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

val generalPoolCommonModule = module {

    // repository
    single<LiveOrdersRepository> {
        LiveOrdersRepositoryImpl(
            get(), get()
        )
    }

    // Use cases
    factory { LoadOrdersUseCase(get<LiveOrdersRepository>()) }
    factory { OrdersRealtimeUseCase(get<LiveOrdersRepository>()) }
    factory { ComputeDistancesUseCase() }

    // Api
    single<LiveOrdersApiService> {
        LiveOrdersApiKtor(
            tokenStore = get()
        )
    }
}
