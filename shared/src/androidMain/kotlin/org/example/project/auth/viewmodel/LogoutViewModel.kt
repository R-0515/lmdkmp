package org.example.project.auth.viewmodel

import androidx.lifecycle.ViewModel
import com.ntg.lmd.network.core.KtorClientProvider
import org.example.project.SecureTokenStore

actual class LogoutViewModel(
    private val store: SecureTokenStore
) : ViewModel() {

    actual fun logout() {
        store.clear() // clear saved tokens
        KtorClientProvider.closeClient()
    }
}