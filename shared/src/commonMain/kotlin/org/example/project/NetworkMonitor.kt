package org.example.project

import kotlinx.coroutines.flow.Flow

expect class NetworkMonitor {
    val isOnline: Flow<Boolean>
}