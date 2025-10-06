package org.lmd.navigation

interface NavigationHandler {
    fun navigateToHome()
    fun navigateToLogin()
    fun showMessage(message: String)
}