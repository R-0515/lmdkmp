package com.ntg.lmd.network.core

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlinx.serialization.json.Json
import org.example.project.auth.data.AuthApi
import org.example.project.AuthPlugin
import org.example.project.BuildKonfig
import org.example.project.SecureTokenStore
import org.example.project.installTokenAuth
import kotlin.concurrent.Volatile

object KtorClientProvider {

    @OptIn(InternalCoroutinesApi::class)
    private val lock = SynchronizedObject()

    // Keep a single shared HttpClient instance (avoid creating a new one each time)
    @Volatile
    private var cachedClient: HttpClient? = null

    @OptIn(InternalCoroutinesApi::class)
    fun getClient(
        store: SecureTokenStore,
        supabaseKey: String,
        refreshApi: AuthApi
    ): HttpClient {
        // Return the existing client if available; otherwise, create a new one once
        return cachedClient ?: synchronized(lock) {
            cachedClient ?: createClient(store, supabaseKey, refreshApi).also {
                cachedClient = it
            }
        }
    }

    // Builds the HttpClient with all required plugins
    private fun createClient(
        store: SecureTokenStore,
        supabaseKey: String,
        refreshApi: AuthApi
    ): HttpClient {
        return HttpClient {
            // JSON serialization configuration
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    isLenient = true
                })
            }

            // Enable logging for debugging HTTP requests/responses
            install(Logging) {
                level = LogLevel.ALL
            }

            // Default base URL for all requests
            install(DefaultRequest) {
                url {
                    takeFrom(BuildKonfig.BASE_URL)
                }
            }

            // Install custom authentication and plugins
            installTokenAuth(store, refreshApi)
            install(AuthPlugin(store, supabaseKey))
        }
    }

    // Optional: safely close the client when the app shuts down
    fun closeClient() {
        cachedClient?.close()
        cachedClient = null
    }
}
