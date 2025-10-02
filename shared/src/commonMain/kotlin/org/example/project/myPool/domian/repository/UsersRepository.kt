package org.example.project.myPool.domian.repository

import org.example.project.myPool.domian.model.ActiveUser

interface UsersRepository {
    suspend fun getActiveUsers(): Pair<List<ActiveUser>, String?>
}