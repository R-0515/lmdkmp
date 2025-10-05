package org.example.project.di

import io.ktor.client.HttpClient
import org.example.project.auth.data.AuthApi
import org.example.project.SecureTokenStore
import org.example.project.auth.data.AuthRepositoryImpl
import org.example.project.auth.domain.repository.AuthRepository
import org.example.project.auth.domain.usecase.LoginUseCase
import org.example.project.location.data.repository.LocationRepositoryImpl
import org.example.project.location.domain.repository.LocationRepository
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase
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

