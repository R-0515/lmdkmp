package org.example.project

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import org.example.project.auth.data.AuthApi
import org.example.project.auth.data.model.LoginData
import org.example.project.auth.data.model.RefreshTokenRequest
import org.example.project.auth.data.model.ApiResponse

fun HttpClientConfig<*>.installTokenAuth(
    store: SecureTokenStore,
    refreshApi: AuthApi
) {
    install(Auth) {
        bearer {
            // Load current tokens from storage
            loadTokens {
                val access = store.getAccessToken()
                val refresh = store.getRefreshToken()
                if (access != null && refresh != null) {
                    BearerTokens(access, refresh)
                } else {
                    null
                }
            }

            // Refresh tokens when a 401 occurs
            refreshTokens {
                val rt = store.getRefreshToken() ?: return@refreshTokens null

                // Call API to refresh tokens
                val response: ApiResponse<LoginData> =
                    refreshApi.refreshToken(RefreshTokenRequest(rt))

                return@refreshTokens if (response.success && response.data != null) {
                    val data: LoginData = response.data

                    // Save new tokens
                    store.saveFromPayload(
                        data.accessToken,
                        data.refreshToken,
                        data.expiresAt,
                        data.refreshExpiresAt
                    )

                    BearerTokens(
                        data.accessToken ?: return@refreshTokens null,
                        data.refreshToken ?: return@refreshTokens null
                    )
                } else {
                    // Clear store if refresh fails
                    store.clear()
                    null
                }
            }
        }
    }
}
