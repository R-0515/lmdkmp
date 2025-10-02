package org.example.project.myPool.ui.model

sealed class MyPoolLoadResult {
    data class Error(
        val throwable: Throwable,
    ) : MyPoolLoadResult()
}