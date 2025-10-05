package org.lmd.project.auth.data

import org.lmd.project.SecureTokenStore
import org.lmd.project.auth.data.model.ApiResponse
import org.lmd.project.auth.data.model.LoginData
import org.lmd.project.auth.domain.repository.AuthRepository
import org.lmd.project.core.utils.ApiResult

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val store: SecureTokenStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<LoginData> {
        return try {
            val response: ApiResponse<LoginData> = authApi.login(email, password)

            if (response.success && response.data != null) {
                val data = response.data

                // Save tokens only if available (refresh-token call)
                store.saveFromPayload(
                    access = data.accessToken,
                    refresh = data.refreshToken,
                    expiresAt = data.expiresAt,
                    refreshExpiresAt = data.refreshExpiresAt
                )


                ApiResult.Success(data)
            } else {
                ApiResult.Error(response.error ?: "Invalid email or password")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }
}
