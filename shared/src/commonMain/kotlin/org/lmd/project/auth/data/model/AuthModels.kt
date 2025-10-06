package org.lmd.project.auth.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)


@Serializable
data class LoginData(
    val user: LoginUser? = null,
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("refresh_expires_at") val refreshExpiresAt: String? = null
)



@Serializable
data class LoginUser(
    val id: String,
    val email: String,
    val full_name: String? = null
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)


