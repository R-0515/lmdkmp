package org.example.project.util

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

actual fun Throwable.platformMessage(): String = when (this) {
    is UnknownHostException, is ConnectException -> "No internet connection."
    is SocketTimeoutException -> "Request timed out. Please try again."
    is SSLHandshakeException -> "Secure connection failed."
    else -> "Unexpected error. Please try again."
}