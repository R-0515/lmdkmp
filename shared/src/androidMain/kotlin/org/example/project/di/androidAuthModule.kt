package org.example.project.di

import com.ntg.lmd.network.core.KtorClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.BuildKonfig
import org.example.project.SecureTokenStore
import org.example.project.SecureTokenStoreImpl
import org.example.project.auth.data.AuthApi
import org.example.project.auth.data.AuthRepositoryImpl
import org.example.project.auth.domain.repository.AuthRepository
import org.example.project.auth.domain.usecase.LoginUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// in your DI module
import org.koin.core.qualifier.named

val androidAuthModule = module {
    single<SecureTokenStore> { SecureTokenStoreImpl(androidContext()) }

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


    factory { LoginUseCase(get()) }
}
