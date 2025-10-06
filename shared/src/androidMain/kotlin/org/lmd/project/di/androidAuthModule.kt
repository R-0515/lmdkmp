package org.lmd.project.di

import com.ntg.lmd.network.core.KtorClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.lmd.project.BuildKonfig
import org.lmd.project.SecureTokenStore
import org.lmd.project.SecureTokenStoreImpl
import org.lmd.project.UserStore
import org.lmd.project.auth.data.AuthApi
import org.lmd.project.auth.data.AuthRepositoryImpl
import org.lmd.project.auth.domain.repository.AuthRepository
import org.lmd.project.auth.domain.usecase.LoginUseCase
import org.lmd.project.auth.viewmodel.LoginViewModel
import org.lmd.project.auth.viewmodel.LogoutViewModel
import org.lmd.project.utils.AndroidUserStore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// in your DI module
import org.koin.core.qualifier.named

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
        KtorClientProvider.getClient(
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

    viewModel { LogoutViewModel(store = get()) }

    viewModel { LoginViewModel(get()) }
}
