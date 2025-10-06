package org.lmd.project

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.example.project.DeliveriesLogScreen
import org.lmd.project.generalPool.generalPoolScreen
import org.lmd.project.navigation.NavigationHandler
import org.lmd.project.auth.ui.EmptyScreen
import org.lmd.project.auth.ui.LoginScreen

@Composable
fun AppNavGraph(navController: androidx.navigation.NavHostController, navigationHandler: NavigationHandler) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navigationHandler = navigationHandler)
        }

        composable("empty") {
            EmptyScreen(navigationHandler = navigationHandler)
        }
        composable("deliveriesLog") { DeliveriesLogScreen(navigationHandler = navigationHandler) }
        composable("orderHistory") { OrdersHistoryScreen(navigationHandler = navigationHandler) }
        composable("generalPool") { generalPoolScreen(navigationHandler = navigationHandler) }

    }
}