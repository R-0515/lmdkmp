package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        // Replace demo UI with your network status screen
        val monitor = NetworkMonitor(LocalContext.current)
        NetworkStatusScreen(monitor)
    }
}