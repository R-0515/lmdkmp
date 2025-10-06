package org.lmd.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import org.lmd.project.navigation.NavigationHandlerImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val monitor = NetworkMonitor(this)
        setContent {
            val navController = rememberNavController()
            val navigationHandler = NavigationHandlerImpl(this, navController)
            MaterialTheme {
                //NetworkStatusScreen(monitor)
                //MapScreen()
                //LoginScreen()

                AppNavGraph(navController, navigationHandler)
                }
            }
        }
    }
}


