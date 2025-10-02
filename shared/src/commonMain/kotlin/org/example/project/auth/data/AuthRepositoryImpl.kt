package org.example.project.auth.data

import org.example.project.SecureTokenStore
import org.example.project.auth.data.model.ApiResponse
import org.example.project.auth.data.model.LoginData
import org.example.project.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val store: SecureTokenStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResponse<LoginData> {
        val response = authApi.login(email, password)

        if (response.success && response.data != null) {
            response.data?.let { data ->
                store.saveFromPayload(
                    access = data.accessToken,
                    refresh = data.refreshToken,
                    expiresAt = data.expiresAt,
                    refreshExpiresAt = data.refreshExpiresAt
                )
            }
        }
        return response
    }
}

