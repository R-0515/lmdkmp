package org.example.project.di

import org.example.project.delivery.data.api.DeliveriesLogApi
import org.example.project.delivery.data.api.DeliveriesLogApiKtor
import org.example.project.delivery.data.repositoryimpl.DeliveriesLogRepositoryImpl
import org.example.project.delivery.domain.repository.DeliveriesLogRepository
import org.example.project.delivery.domain.usecase.GetDeliveriesLogPageUseCase
import org.example.project.location.data.repository.LocationRepositoryImpl
import org.example.project.location.domain.repository.LocationRepository
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase
import org.koin.dsl.module

val locationCommonModule = module {
    // Repository API (depends on LocationProvider)
    single<LocationRepository> { LocationRepositoryImpl(get()) }

    // UseCase (depends on Repository)
    factory { GetDeviceLocationsUseCase(get()) }
}

val deliveryModule = module {
    single<DeliveriesLogApi> { DeliveriesLogApiKtor() }
    single<DeliveriesLogRepository> { DeliveriesLogRepositoryImpl(get()) }
    factory { GetDeliveriesLogPageUseCase(get()) }
}