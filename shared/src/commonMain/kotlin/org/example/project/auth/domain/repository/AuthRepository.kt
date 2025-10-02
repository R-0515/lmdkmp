package org.example.project.auth.domain.repository

import org.example.project.auth.data.model.LoginData
import org.example.project.core.utils.ApiResult

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<LoginData>
}