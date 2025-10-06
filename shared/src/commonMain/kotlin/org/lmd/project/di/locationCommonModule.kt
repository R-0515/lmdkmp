package org.lmd.project.di

import org.lmd.project.auth.domain.usecase.LoginUseCase
import org.koin.dsl.module

//val locationCommonModule = module {
//    // Repository API (depends on LocationProvider)
//    single<LocationRepository> { LocationRepositoryImpl(get()) }
//
//    // UseCase (depends on Repository)
//    factory { GetDeviceLocationsUseCase(get()) }
//}

// shared/commonMain
val authCommonModule = module {

    // UseCase
    factory { LoginUseCase(get(), get()) }
}

