package org.example.project.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.navigation.NavigationHandler
import org.example.project.auth.viewmodel.LogoutViewModel
import org.koin.compose.koinInject

@Composable
fun EmptyScreen(
    navigationHandler: NavigationHandler,
    viewModel: LogoutViewModel = koinInject()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome! 🎉")
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.logout()
                    navigationHandler.navigateToLogin() // use shared interface
                }
            ) {
                Text("Logout")
            }
        }
    }
}
