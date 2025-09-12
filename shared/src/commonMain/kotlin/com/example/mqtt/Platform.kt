package com.example.mqtt

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform