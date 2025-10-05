package org.example.project.auth.viewmodel

import androidx.lifecycle.ViewModel
import com.ntg.lmd.network.core.KtorClientProvider
import org.example.project.SecureTokenStore

class LogoutViewModel(
    private val store: SecureTokenStore
) : ViewModel() {

    fun logout() {
        store.clear() // clear token
        KtorClientProvider.closeClient() // optional: reset client cache
    }
}