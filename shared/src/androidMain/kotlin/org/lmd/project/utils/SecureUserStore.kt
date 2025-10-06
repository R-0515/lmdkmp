package org.lmd.project.utils

import android.content.Context
import org.lmd.project.UserStore

class AndroidUserStore(context: Context) : UserStore {

    private val secureStore = SecurePreferencesStore(context)

    override fun getUserId(): String? = secureStore.getUserId()

    override fun saveUser(id: String?, email: String?, fullName: String?) {
        secureStore.saveUser(id, email, fullName)
    }

    override fun clear() = secureStore.clear()
}
