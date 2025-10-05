package org.example.project.generalPool.data.datasource.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createPlatformHttpClient(): HttpClient =
    HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
                followSslRedirects(true)
            }
        }
    }
