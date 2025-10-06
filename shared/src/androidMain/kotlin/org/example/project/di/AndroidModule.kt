
package org.example.project.di

import android.os.Build
import androidx.annotation.RequiresExtension
import com.google.android.gms.location.LocationServices
import com.ntg.lmd.network.core.KtorClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.BuildKonfig
import org.example.project.NetworkMonitor
import org.example.project.SecureTokenStore
import org.example.project.SecureTokenStoreImpl
import org.example.project.UserStore
import org.example.project.auth.data.AuthApi
import org.example.project.auth.data.AuthRepositoryImpl
import org.example.project.auth.domain.repository.AuthRepository
import org.example.project.auth.domain.usecase.LoginUseCase
import org.example.project.location.data.repository.AndroidLocationProvider
import org.example.project.location.domain.model.ComputeDistancesUseCase
import org.example.project.location.domain.repository.LocationProvider
import org.example.project.myPool.ui.viewmodel.ActiveAgentsViewModel
import org.example.project.myPool.ui.viewmodel.MyOrdersViewModel
import org.example.project.myPool.ui.viewmodel.MyPoolViewModel
import org.example.project.myPool.ui.viewmodel.UpdateOrderStatusViewModel
import org.example.project.util.AndroidUserStore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val androidAuthModule = module {
    single<SecureTokenStore> { SecureTokenStoreImpl(androidContext()) }

    // Secure user store
    single<UserStore> { AndroidUserStore(androidContext()) }

    // Base client (no auth) - only for refresh
    single<HttpClient>(named("baseClient")) {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    isLenient = true
                })
            }
        }
    }

    // Refresh API uses base client
    single<AuthApi>(named("refreshApi")) {
        AuthApi(get(named("baseClient")))
    }

    // Main client with token auth & logging
    single<HttpClient>(named("mainClient")) {
        KtorClientProvider.create(
            store = get(),
            supabaseKey = BuildKonfig.SUPABASE_KEY,
            refreshApi = get(named("refreshApi"))
        )
    }

    // Main AuthApi (for login etc.)
    single<AuthApi> { AuthApi(get(named("mainClient"))) }

    // Repository
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }


    factory { LoginUseCase(get(), get()) }
}

val locationAndroidModule = module {
    // FusedLocationProviderClient (needs Context)
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    // Provider API (callback/suspend style)
    single<LocationProvider> { AndroidLocationProvider(get()) }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
val MyOrderMyPoolModule =
    module {
        factory { ComputeDistancesUseCase() }

        // ViewModels
        viewModel {
            MyOrdersViewModel(
                get(),
                get(),
                get(),
            )
        }
        viewModel {
            MyPoolViewModel(
                get(),
                get(),
            )
        }
        viewModel {
            UpdateOrderStatusViewModel(
                get(),
                get(),
            )
        }
        viewModel { ActiveAgentsViewModel(get()) }
    }

val SecureTokenAndroidModule = module {

    // Bind SecureTokenStore to SecureTokenStoreImpl
    single<SecureTokenStore> { SecureTokenStoreImpl(androidContext()) }

    // Bind NetworkMonitor
    single { NetworkMonitor(androidContext()) }
}