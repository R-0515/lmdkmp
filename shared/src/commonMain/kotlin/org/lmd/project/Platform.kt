package org.lmd.project

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform