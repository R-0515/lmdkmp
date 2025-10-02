package org.example.project.auth.domain.usecase

import org.example.project.auth.data.model.LoginData
import org.example.project.auth.domain.repository.AuthRepository
import org.example.project.core.utils.ApiResult

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): ApiResult<LoginData> {
        return repository.login(email, password)
    }
}
