package org.example.project.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import org.example.project.location.data.repository.LocationRepositoryImpl
import org.example.project.location.domain.repository.LocationRepository
import org.example.project.location.domain.usecase.GetDeviceLocationsUseCase
import org.koin.dsl.module
import org.example.project.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.example.project.socket.SocketIntegration
import org.example.project.BuildKonfig


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

