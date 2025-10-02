package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import org.example.project.auth.ui.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val monitor = NetworkMonitor(this)

        setContent {
            MaterialTheme {
                //NetworkStatusScreen(monitor)
                //MapScreen()
                LoginScreen()
            }
        }
    }
}


