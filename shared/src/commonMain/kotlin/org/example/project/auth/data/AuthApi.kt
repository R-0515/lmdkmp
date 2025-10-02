package org.example.project.auth.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.BuildKonfig
import org.example.project.auth.data.model.ApiResponse
import org.example.project.auth.data.model.LoginData
import org.example.project.auth.data.model.LoginRequest
import org.example.project.auth.data.model.RefreshTokenRequest

class AuthApi(private val client: HttpClient) {
    private val baseUrl: String = BuildKonfig.BASE_URL

    suspend fun refreshToken(request: RefreshTokenRequest): ApiResponse<LoginData> {
        return client.post("$baseUrl/refresh-token") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun login(email: String, password: String): ApiResponse<LoginData> {
        return client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password)) // now serializable by the installed plugin
        }.body()
    }
}
