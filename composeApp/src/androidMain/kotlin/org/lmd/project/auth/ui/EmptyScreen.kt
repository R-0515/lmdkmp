package org.lmd.project.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.lmd.project.auth.viewmodel.LogoutViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun EmptyScreen(
    navController : NavController,
    viewModel: LogoutViewModel = koinViewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Welcome! 🎉")
    }
    Button(
        onClick = {
            viewModel.logout()
            navController.navigate("login") {
                popUpTo("empty") { inclusive = true }
            }
        }
    ) {
        Text("Logout")
    }
}
