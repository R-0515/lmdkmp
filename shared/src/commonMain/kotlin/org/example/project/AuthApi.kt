package org.example.project

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApi(private val client: HttpClient, private val baseUrl: String) {

    suspend fun refreshToken(request: RefreshTokenRequest): ApiResponse<LoginData> {
        return client.post("$baseUrl/refresh-token") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun login(username: String, password: String): ApiResponse<LoginData> {
        return client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("username" to username, "password" to password))
        }.body()
    }
}