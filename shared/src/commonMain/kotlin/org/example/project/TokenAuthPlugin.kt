package org.example.project

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer

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
                } else null
            }

            // Refresh tokens when 401 happens
            refreshTokens {
                val rt = store.getRefreshToken() ?: return@refreshTokens null
                val res = refreshApi.refreshToken(RefreshTokenRequest(rt))
                if (res.success) {
                    val data = res.data
                    store.saveFromPayload(
                        data?.accessToken,
                        data?.refreshToken,
                        data?.expiresAt,
                        data?.refreshExpiresAt
                    )
                    BearerTokens(data?.accessToken!!, data.refreshToken!!)
                } else {
                    store.clear()
                    null
                }
            }
        }
    }
}