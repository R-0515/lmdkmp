package org.example.project.auth.domain.usecase

import org.example.project.UserStore
import org.example.project.auth.data.model.LoginData
import org.example.project.auth.domain.repository.AuthRepository
import org.example.project.core.utils.ApiResult

class LoginUseCase(
    private val repository: AuthRepository,
    private val userStore: UserStore
) {
    suspend operator fun invoke(email: String, password: String): ApiResult<LoginData> {
        val result = repository.login(email, password)

        if (result is ApiResult.Success) {
            val user = result.data?.user
            if (user != null) {
                userStore.saveUser(
                    id = user.id,
                    email = user.email,
                    fullName = user.full_name
                )
            }
        }

        return result
    }
}