package org.example.project
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.example.navigation.NavigationHandler
import org.example.project.auth.ui.EmptyScreen
import org.example.project.auth.ui.LoginScreen

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
    }
}