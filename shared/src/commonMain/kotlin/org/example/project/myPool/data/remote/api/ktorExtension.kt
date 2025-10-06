package org.example.project.myPool.data.remote.api

import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

internal class HttpError(val code: Int, message: String): RuntimeException(message)

internal inline fun ensureSuccess(status: HttpStatusCode, body: String) {
    if (!status.isSuccess()) {
        throw HttpError(status.value, "HTTP ${status.value}: $body")
    }
}
