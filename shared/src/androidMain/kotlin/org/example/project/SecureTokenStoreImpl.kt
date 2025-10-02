package org.example.project

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// androidMain
class SecureTokenStoreImpl(ctx: Context) : SecureTokenStore {
    private val sp = EncryptedSharedPreferences.create(
        ctx,
        "secure_auth_prefs",
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun getAccessToken(): String? = sp.getString("access", null)
    override fun getRefreshToken(): String? = sp.getString("refresh", null)
    override fun getAccessExpiryIso(): String? = sp.getString("access_exp", null)
    override fun getRefreshExpiryIso(): String? = sp.getString("refresh_exp", null)

    override fun saveFromPayload(access: String?, refresh: String?, expiresAt: String?, refreshExpiresAt: String?) {
        sp.edit().apply {
            putString("access", access)
            putString("refresh", refresh ?: getRefreshToken())
            putString("access_exp", expiresAt)
            putString("refresh_exp", refreshExpiresAt ?: getRefreshExpiryIso())
        }.apply()
    }

    override fun saveTokens(access: String?, refresh: String?) {
        sp.edit().putString("access", access).putString("refresh", refresh).apply()
    }

    override fun clear() = sp.edit().clear().apply()
}
