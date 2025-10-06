package org.lmd.project

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import org.lmd.project.auth.data.AuthApi
import org.lmd.project.auth.data.model.RefreshTokenRequest

fun HttpClientConfig<*>.installTokenAuth(store: SecureTokenStore, refreshApi: AuthApi) {
    install(Auth) {
        bearer {
            loadTokens {
                val access = store.getAccessToken()
                val refresh = store.getRefreshToken()
                if (access != null && refresh != null) {
                    BearerTokens(access, refresh)
                } else null
            }
            refreshTokens {
                val rt = store.getRefreshToken() ?: return@refreshTokens null
                val response = refreshApi.refreshToken(RefreshTokenRequest(rt))
                if (response.success && response.data != null) {
                    val data = response.data
                    store.saveFromPayload(
                        data.accessToken,
                        data.refreshToken,
                        data.expiresAt,
                        data.refreshExpiresAt
                    )
                    BearerTokens(data.accessToken!!, data.refreshToken!!)
                } else {
                    store.clear()
                    null
                }
            }
        }
    }
}
