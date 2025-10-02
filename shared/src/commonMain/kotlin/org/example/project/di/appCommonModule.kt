package org.example.project.di

import org.example.project.generalPool.data.datasource.remote.LiveOrdersApiService
import org.example.project.location.data.repository.LocationRepositoryImpl
import org.example.project.location.domain.repository.LocationRepository
import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase
import org.example.project.generalPool.data.repository.LiveOrdersRepositoryImpl
import org.example.project.generalPool.domain.repository.LiveOrdersRepository
import org.example.project.generalPool.domain.usecase.LoadOrdersUseCase
import org.example.project.generalPool.domain.usecase.OrdersRealtimeUseCase
import org.koin.dsl.module

val locationCommonModule = module {
    // Repository API (depends on LocationProvider)
    single<LocationRepository> { LocationRepositoryImpl(get()) }

    // UseCase (depends on Repository)
    factory { GetDeviceLocationsUseCase(get()) }
}

val generalPoolCommonModule = module {

    // repository
    single<LiveOrdersRepository> { LiveOrdersRepositoryImpl(get()) }

    // Use cases
    factory { LoadOrdersUseCase(get<LiveOrdersRepository>()) }
    factory { OrdersRealtimeUseCase(get<LiveOrdersRepository>()) }
    factory { ComputeDistancesUseCase() }

    // Api
    single {
        LiveOrdersApiService(
            client = get(),
            baseUrl = getProperty("BASE_URL")
        )
    }
}