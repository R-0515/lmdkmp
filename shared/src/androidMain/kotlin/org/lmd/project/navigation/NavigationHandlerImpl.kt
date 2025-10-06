package org.lmd.project.navigation

import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController

class NavigationHandlerImpl(
    private val context: Context,
    private val navController: NavController
) : NavigationHandler {

    override fun navigateToHome() {
        navController.navigate("empty") {
            popUpTo("login") { inclusive = true }
        }
    }

    override fun navigateToLogin() {
        navController.navigate("login") {
            popUpTo("empty") { inclusive = true }
        }
    }

    override fun navigateToOrderHistory() {
        navController.navigate("orderHistory") {
            popUpTo("login") { inclusive = true }
        }
    }

    override fun navigateToDelivery() {
        navController.navigate("deliveriesLog") {
            popUpTo("login") { inclusive = true }
        }
    }

    override fun navigateToGeneralPool() {
        navController.navigate("generalPool") {
            popUpTo("login") { inclusive = true }
        }
    }

    override fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
