package com.ntg.lmd.network.core

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.auth.data.AuthApi
import org.example.project.AuthPlugin
import org.example.project.BuildKonfig
import org.example.project.SecureTokenStore
import org.example.project.installTokenAuth

object KtorClientProvider {
    fun create(
        store: SecureTokenStore,
        supabaseKey: String,
        refreshApi: AuthApi
    ): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    isLenient = true
                })
            }

            install(Logging) {
                level = LogLevel.ALL
            }

            install(DefaultRequest) {
                url {
                    takeFrom(BuildKonfig.BASE_URL)
                }
            }

            installTokenAuth(store, refreshApi)
            install(AuthPlugin(store, supabaseKey))
        }
    }
}
