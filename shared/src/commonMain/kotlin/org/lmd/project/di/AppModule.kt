package org.lmd.project.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.CoroutineDispatcher
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
import org.lmd.project.BuildKonfig
import org.lmd.project.SecureTokenStore
import org.lmd.project.socket.ConsoleLogger
import org.lmd.project.socket.Logger
import org.lmd.project.socket.SocketConfig


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

