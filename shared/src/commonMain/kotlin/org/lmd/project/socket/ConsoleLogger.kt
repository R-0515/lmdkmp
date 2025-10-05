package org.lmd.project.socket

class ConsoleLogger : Logger {
    override fun d(tag: String, msg: String) {
        println("D/$tag: $msg")
    }

    override fun w(tag: String, msg: String) {
        println("W/$tag: $msg")
    }
}
