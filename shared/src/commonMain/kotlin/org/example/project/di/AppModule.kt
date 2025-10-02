package org.example.project.di

import org.example.project.location.data.repository.LocationRepositoryImpl
import org.example.project.location.domain.repository.LocationRepository
import org.example.project.location.domain.usecase.ComputeDistancesUseCase
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase
import org.example.project.myPool.data.repository.MyOrdersRepositoryImpl
import org.example.project.myPool.data.repository.UpdateOrdersStatusRepositoryImpl
import org.example.project.myPool.data.repository.UsersRepositoryImpl
import org.example.project.myPool.domian.repository.MyOrdersRepository
import org.example.project.myPool.domian.repository.UpdateOrdersStatusRepository
import org.example.project.myPool.domian.repository.UsersRepository
import org.example.project.myPool.domian.usecase.GetActiveUsersUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.domian.usecase.UpdateOrderStatusUseCase
import org.koin.dsl.module

val locationCommonModule = module {
    // Repository API (depends on LocationProvider)
    single<LocationRepository> { LocationRepositoryImpl(get()) }

    // UseCase (depends on Repository)
    factory { GetDeviceLocationsUseCase(get()) }
}

val myOrderMyPoolModule = module {
    // Repos
    single<MyOrdersRepository> { MyOrdersRepositoryImpl(get()) }
    single<UpdateOrdersStatusRepository> { UpdateOrdersStatusRepositoryImpl(get()) }
    single<UsersRepository> { UsersRepositoryImpl(get()) }

    // UseCases
    factory { GetMyOrdersUseCase(get()) }
    factory { UpdateOrderStatusUseCase(get()) }
    factory { GetActiveUsersUseCase(get()) }
    factory { ComputeDistancesUseCase() }
}