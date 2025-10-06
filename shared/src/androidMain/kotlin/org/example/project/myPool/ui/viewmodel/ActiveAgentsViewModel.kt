package org.example.project.myPool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.example.project.myPool.domian.usecase.GetActiveUsersUseCase
import org.example.project.myPool.ui.logic.ActiveAgentsLogic

class ActiveAgentsViewModel(
    getActiveUsers: GetActiveUsersUseCase,
) : ViewModel() {

    private val delegate = ActiveAgentsLogic(
        getActiveUsers = getActiveUsers,
        scope = viewModelScope
    )

    val state = delegate.state

    fun load() = delegate.load()
}