package org.example.project.delivery.data.api



internal class HttpError(val code: Int, message: String): RuntimeException(message)

//internal inline fun ensureSuccess(status: HttpStatusCode, body: String) {
//    if (!status.isSuccess()) {
//        throw HttpError(status.value, "HTTP ${status.value}: $body")
//    }
//}