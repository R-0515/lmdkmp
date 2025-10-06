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
import org.example.project.auth.domain.usecase.LoginUseCase
import org.example.project.myPool.data.remote.api.GetUsersApi
import org.example.project.myPool.data.remote.api.OrdersApi
import org.example.project.myPool.data.remote.api.UpdateOrderStatusApi
import org.example.project.myPool.data.repository.MyOrdersRepositoryImpl
import org.example.project.myPool.data.repository.UpdateOrdersStatusRepositoryImpl
import org.example.project.myPool.data.repository.UsersRepositoryImpl
import org.example.project.myPool.domian.repository.MyOrdersRepository
import org.example.project.myPool.domian.repository.UpdateOrdersStatusRepository
import org.example.project.myPool.domian.repository.UsersRepository
import org.example.project.myPool.domian.usecase.GetActiveUsersUseCase
import org.example.project.myPool.domian.usecase.GetMyOrdersUseCase
import org.example.project.myPool.domian.usecase.UpdateOrderStatusUseCase

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
            baseWsUrl = BuildKonfig.WS_BASE_URL,
            client = get(),
            tokenStore = get()
        )
    }
}
val MyOrderMyPoolModuleRepos =
    module {
        //Api
        single { OrdersApi(tokenStore = get()) }
        single { UpdateOrderStatusApi(tokenStore = get()) }
        single { GetUsersApi(tokenStore = get()) }

        // Repos
        single<MyOrdersRepository> { MyOrdersRepositoryImpl(get()) }
        single<UpdateOrdersStatusRepository> { UpdateOrdersStatusRepositoryImpl(get()) }
        single<UsersRepository> { UsersRepositoryImpl(get()) }

        // UseCases
        factory { GetMyOrdersUseCase(get()) }
        factory { UpdateOrderStatusUseCase(get()) }
        factory { GetActiveUsersUseCase(get()) }

    }

// shared/commonMain
val authCommonModule = module {

    // UseCase
    factory { LoginUseCase(get(), get()) }
}

