package org.example.project.myPool.ui.model

import org.example.project.myPool.domian.model.ActiveUser

data class AgentsState(
    val isLoading: Boolean = false,
    val agents: List<ActiveUser> = emptyList(),
    val error: String? = null,
    val currentUserId: String? = null,
)