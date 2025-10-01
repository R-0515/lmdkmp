package org.example.project.socket

fun interface SecureTokenStore {
     fun getAccessToken(): String?
}