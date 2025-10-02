package org.example.project.auth.domain.repository

import org.example.project.auth.data.model.ApiResponse
import org.example.project.auth.data.model.LoginData

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResponse<LoginData>
}