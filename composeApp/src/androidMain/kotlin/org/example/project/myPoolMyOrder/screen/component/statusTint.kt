package org.example.project.myPoolMyOrder.screen.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun statusTint(status: String) =
    if (status.equals("confirmed", ignoreCase = true) ||
        status.equals("added", ignoreCase = true)
    ) {
        SuccessGreen
    } else {
        MaterialTheme.colorScheme.onSurface
    }
val SuccessGreen = Color(0xFF3BA864)
