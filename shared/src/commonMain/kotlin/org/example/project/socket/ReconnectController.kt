package org.example.project.socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class ReconnectController(
    private val scope: CoroutineScope,
    private val action: () -> Unit,
) {
    private val logTag = "LMD-WS"
    private var job: Job? = null

    fun schedule(delayMs: Long) {
        cancel()
        job = scope.launch {
            logD(logTag, "Reconnection scheduled in ${delayMs}ms")
            delay(delayMs)
            logD(logTag, "Auto-reconnecting…")
            action()
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
    }
}

private inline fun logD(tag: String, msg: String) = println("D/$tag: $msg")
