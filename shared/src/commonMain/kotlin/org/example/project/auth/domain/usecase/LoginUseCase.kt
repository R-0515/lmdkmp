package org.example.project.auth.domain.usecase

import org.example.project.auth.data.model.ApiResponse
import org.example.project.auth.data.model.LoginData
import org.example.project.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): ApiResponse<LoginData> {
        return repository.login(email, password)
    }
}