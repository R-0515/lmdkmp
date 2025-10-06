package org.lmd.project

interface UserStore {
    fun getUserId(): String?
    fun saveUser(id: String?, email: String?, fullName: String?)
    fun clear()
}
