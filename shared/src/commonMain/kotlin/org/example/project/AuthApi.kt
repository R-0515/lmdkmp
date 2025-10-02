package org.example.project

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.BuildKonfig

class AuthApi(private val client: HttpClient) {
    private val baseUrl: String = BuildKonfig.BASE_URL


    suspend fun refreshToken(request: RefreshTokenRequest): ApiResponse<LoginData> {
        return client.post("$baseUrl/refresh-token") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun login(username: String, password: String): ApiResponse<LoginData> {
        return client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }.body()
    }
}