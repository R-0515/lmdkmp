package org.example.project.myPool.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.example.project.myPool.domian.usecase.GetActiveUsersUseCase
import org.example.project.myPool.ui.logic.ActiveAgentsLogic

class ActiveAgentsViewModel(
    getActiveUsers: GetActiveUsersUseCase,
) : ViewModel() {

    val logic = ActiveAgentsLogic(getActiveUsers, viewModelScope)
}