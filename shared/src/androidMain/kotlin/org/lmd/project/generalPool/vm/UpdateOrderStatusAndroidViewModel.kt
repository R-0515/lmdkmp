package org.lmd.project.generalPool.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.lmd.project.generalPool.domain.usecase.UpdateOrderStatusUseCase

class UpdateOrderStatusAndroidViewModel(
    updateStatus: UpdateOrderStatusUseCase
) : ViewModel() {

    val presenter = UpdateOrderStatusController(
        updateStatus = updateStatus,
        scope = viewModelScope
    )

    override fun onCleared() {
        super.onCleared()
        presenter.clear()
    }
}
