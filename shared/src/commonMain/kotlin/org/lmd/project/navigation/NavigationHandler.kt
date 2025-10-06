package org.lmd.project.navigation

interface NavigationHandler {
    fun navigateToHome()
    fun navigateToLogin()
    fun navigateToOrderHistory()
    fun navigateToDelivery()
    fun showMessage(message: String)
}