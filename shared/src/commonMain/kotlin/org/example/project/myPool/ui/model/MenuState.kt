package org.example.project.myPool.ui.model


data class MenuState(
    val expanded: Boolean,
    val onExpand: () -> Unit,
    val onDismiss: () -> Unit,
    val onPickUp: () -> Unit,
    val onCancel: () -> Unit,
    val onReassign: () -> Unit,
)