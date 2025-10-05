package org.example.project.generalPool.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal expect fun createPlatformHttpClient(): HttpClient

internal fun baseHttpClient(
    apikeyProvider: () -> String,
    authProvider: () -> String,
): HttpClient =
    createPlatformHttpClient().config {

        defaultRequest {
            val apikey = apikeyProvider().trim()
            val auth = authProvider().trim()
            if (apikey.isNotEmpty()) header("apikey", apikey)
            if (auth.isNotEmpty()) header(HttpHeaders.Authorization, auth)

            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 20_000
            socketTimeoutMillis = 30_000
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
