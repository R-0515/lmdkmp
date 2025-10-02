package org.example.project.auth.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.BuildKonfig
import org.example.project.auth.data.model.ApiResponse
import org.example.project.auth.data.model.LoginData
import org.example.project.auth.data.model.LoginRequest
import org.example.project.auth.data.model.RefreshTokenRequest

class AuthApi(private val client: HttpClient) {
    private val baseUrl = BuildKonfig.BASE_URL
    private val apiKey = BuildKonfig.SUPABASE_KEY

    suspend fun login(email: String, password: String): ApiResponse<LoginData> {
        val rawResponse = client.post("${baseUrl}login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
            header("apikey", BuildKonfig.SUPABASE_KEY)
            // جرّب أول بدون Authorization
            // header("Authorization", "Bearer ${BuildKonfig.SUPABASE_KEY}")
        }

        val rawText = rawResponse.bodyAsText()
        println("RAW LOGIN RESPONSE: $rawText")

        return rawResponse.body()
    }



    suspend fun refreshToken(request: RefreshTokenRequest): ApiResponse<LoginData> {
        return client.post("${baseUrl}refresh-token") {
            contentType(ContentType.Application.Json)
            setBody(request)
            header("apikey", apiKey)
        }.body()
    }
}
