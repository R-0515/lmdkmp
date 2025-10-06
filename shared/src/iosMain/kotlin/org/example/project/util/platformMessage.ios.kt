package org.example.project.util

actual fun Throwable.platformMessage(): String = when (this) {
    else -> "Unexpected error (iOS). Please try again."}
