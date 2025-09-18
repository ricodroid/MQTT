package com.example.mqtt

interface MqttClient {
    suspend fun connect(
        host: String,
        port: Int = 1883,
        tls: Boolean = false,
        clientId: String? = null,
        username: String? = null,
        password: String? = null,
        keepAliveSec: Int = 30
    )

    fun subscribe(topic: String, qos: Int = 0, onMessage: (ByteArray) -> Unit)

    suspend fun publish(topic: String, payload: ByteArray, qos: Int = 0, retain: Boolean = false)

    suspend fun disconnect()
}

expect object MqttClientFactory {
    fun create(): MqttClient
}