package org.lmd.project.auth.domain.repository

import org.lmd.project.auth.data.model.LoginData
import org.lmd.project.core.utils.ApiResult

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<LoginData>
}