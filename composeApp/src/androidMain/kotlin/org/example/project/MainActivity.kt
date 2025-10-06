package org.example.project

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.example.project.auth.ui.EmptyScreen
import org.example.project.auth.ui.LoginScreen
import org.example.project.myPoolMyOrder.screen.myOrdersScreen
import org.example.project.myPoolMyOrder.screen.myPoolScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val monitor = NetworkMonitor(this)
        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                //NetworkStatusScreen(monitor)
                //MapScreen()
                //LoginScreen()

                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginScreen(navController) }

                    composable("my_orders_screen") {
                        myOrdersScreen(
                            navController = navController,
                        )
                    }
                    composable("my_pool_screen") {
                        myPoolScreen()
                    }
                }
            }
        }
    }
}
