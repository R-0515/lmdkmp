package org.lmd.project.delivery.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun createPlatformHttpClient(): HttpClient = HttpClient(OkHttp)
