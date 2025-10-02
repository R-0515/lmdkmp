package org.example.project.delivery.data.api

//import io.ktor.client.HttpClient
//import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
//import io.ktor.client.plugins.HttpTimeout
//import io.ktor.client.plugins.logging.LogLevel
//import io.ktor.client.plugins.logging.Logging
//import io.ktor.serialization.kotlinx.json.json
//import kotlinx.serialization.json.Json
//
//internal expect fun createPlatformHttpClient(): HttpClient
//
//internal fun baseHttpClient(): HttpClient =
//    createPlatformHttpClient().config {
//        install(ContentNegotiation) {
//            json(
//                Json {
//                    ignoreUnknownKeys = true
//                    isLenient = true
//                    explicitNulls = false
//                }
//            )
//        }
//        install(HttpTimeout) {
//            requestTimeoutMillis = 30_000
//            connectTimeoutMillis = 20_000
//            socketTimeoutMillis = 30_000
//        }
//        install(Logging) {
//            level = LogLevel.INFO
//        }
//    }
