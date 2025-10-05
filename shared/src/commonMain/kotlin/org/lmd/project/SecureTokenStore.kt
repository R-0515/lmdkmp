package org.lmd.project

interface SecureTokenStore {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getAccessExpiryIso(): String?
    fun getRefreshExpiryIso(): String?
    fun saveFromPayload(access: String?, refresh: String?, expiresAt: String?, refreshExpiresAt: String?)
    fun saveTokens(access: String?, refresh: String?)
    fun clear()
}