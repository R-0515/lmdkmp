package org.example.project

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.encodedPath

fun AuthPlugin(store: SecureTokenStore, supabaseKey: String) = createClientPlugin("AuthPlugin") {
    onRequest { request, _ ->
        val path = request.url.encodedPath
        val isAuthEndpoint = path.endsWith("/login") || path.endsWith("/refresh-token")

        request.headers.append("apikey", supabaseKey)

        if (isAuthEndpoint) {
            request.headers.append("Authorization", "Bearer $supabaseKey")
        } else {
            store.getAccessToken()?.takeIf { it.isNotBlank() }?.let {
                request.headers.append("Authorization", "Bearer $it")
            }
        }
    }
}