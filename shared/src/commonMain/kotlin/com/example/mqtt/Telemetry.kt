package com.example.mqtt

import kotlinx.serialization.Serializable

@Serializable
data class Telemetry(
    val temp: Int,
    val ts: Long,
    val deviceId: String
)