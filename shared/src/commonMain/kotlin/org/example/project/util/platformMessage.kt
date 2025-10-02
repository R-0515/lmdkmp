package org.example.project.util

expect fun Throwable.platformMessage(): String

/*actual fun Throwable.platformMessage(): String = when (this) {
    else -> "Unexpected error (iOS). Please try again."
}*/