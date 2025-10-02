package org.example.project.myPool.data.repository

import org.example.project.myPool.domian.model.ActiveUser
import org.example.project.myPool.domian.repository.UsersRepository


class UsersRepositoryImpl(
    private val api: GetUsersApi,
) : UsersRepository {

    override suspend fun getActiveUsers(): Pair<List<ActiveUser>, String?> {
        val env = api.getActiveUsers()
        if (!env.success) error("Failed to load users")

        val list = env.data?.map { it.toDomain() } ?: emptyList()
        return list to env.currentUserId
    }
}