package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.example.project.auth.ui.EmptyScreen
import org.example.project.auth.ui.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val monitor = NetworkMonitor(this)
        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                //NetworkStatusScreen(monitor)
                //MapScreen()
               // LoginScreen()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") { LoginScreen(navController) }
                    composable("deliveriesLog") { DeliveriesLogScreen() }
                    composable("orderHistory") { OrdersHistoryScreen() }

                }
            }
        }
    }
}


