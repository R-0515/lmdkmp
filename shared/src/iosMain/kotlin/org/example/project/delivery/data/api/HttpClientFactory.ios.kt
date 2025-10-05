package org.example.project.delivery.data.api

import io.ktor.client.HttpClient

internal actual fun createPlatformHttpClient(): HttpClient = HttpClient() {
    // Optional: tweak iOS-specific behaviors (timeouts, caching) here
    // engine { }
}
