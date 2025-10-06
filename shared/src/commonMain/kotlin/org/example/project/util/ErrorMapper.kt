package org.example.project.util

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException


fun Throwable.toUserMessage(): String {
    if (this is CancellationException) throw this

    return when (this) {
        is ClientRequestException -> clientErrorMessage(response.status.value)
        is ServerResponseException -> "Server error. Please try later."
        is RedirectResponseException -> "Unexpected redirect. Please try again."
        is ResponseException -> "HTTP ${this.response.status.value}: ${this.message}"
        else -> this.platformMessage()
    }
}

private fun clientErrorMessage(code: Int): String =
    when (code) {
        400 -> "Invalid request. Please try again."
        401 -> "Session expired. Please sign in again."
        403 -> "You don’t have permission to perform this action."
        404 -> "Resource not found."
        else -> "Client error ($code). Please try again."
    }

