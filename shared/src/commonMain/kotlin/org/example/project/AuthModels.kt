package org.example.project
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class LoginData(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: String? = null,
    val refreshExpiresAt: String? = null
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)
