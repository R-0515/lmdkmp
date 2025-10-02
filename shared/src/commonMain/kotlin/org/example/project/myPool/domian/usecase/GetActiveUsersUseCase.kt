package org.example.project.myPool.domian.usecase

import org.example.project.myPool.domian.model.ActiveUser
import org.example.project.myPool.domian.repository.UsersRepository

class GetActiveUsersUseCase(
    private val repo: UsersRepository,
) {
    suspend operator fun invoke(): Pair<List<ActiveUser>, String?> = repo.getActiveUsers()
}