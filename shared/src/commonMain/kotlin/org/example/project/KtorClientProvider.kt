package com.ntg.lmd.network.core

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.AuthApi
import org.example.project.AuthPlugin
import org.example.project.SecureTokenStore
import org.example.project.installTokenAuth

object KtorClientProvider {

    fun create(
        store: SecureTokenStore,
        supabaseKey: String,
        refreshApi: AuthApi
    ): HttpClient {
        return HttpClient {
            install(ContentNegotiation) { json() }
            installTokenAuth(store, refreshApi)
            install(AuthPlugin(store, supabaseKey))
            // JSON parser
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            // Bearer auth + refresh
            installTokenAuth(store, refreshApi)

            // Custom apikey + Authorization header
            install(AuthPlugin(store, supabaseKey))
        }
    }
}
