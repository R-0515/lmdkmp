package org.example.project

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Simplified placeholder for iOS (you can hook into NWPathMonitor later)
actual class NetworkMonitor {
    actual val isOnline: Flow<Boolean> = flowOf(true) // Always online for now
}