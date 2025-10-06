package org.example.project.myPool.data.remote.api

import io.ktor.client.HttpClient

internal actual fun createPlatformHttpClient(): HttpClient = HttpClient() {
    // Optional: tweak iOS-specific behaviors (timeouts, caching) here
    // engine { }
}