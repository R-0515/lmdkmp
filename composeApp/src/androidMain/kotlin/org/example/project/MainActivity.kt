package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val monitor = NetworkMonitor(this)

        setContent {
            MaterialTheme {
                //NetworkStatusScreen(monitor)
                LoginScreen()
            }
        }
    }
}


