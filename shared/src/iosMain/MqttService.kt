package com.example.mqtt.shared

import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.Subscription
import io.github.davidepianca98.mqtt.packets.Qos
import io.github.davidepianca98.mqtt.packets.mqttv5.SubscriptionOptions
import io.github.davidepianca98.socket.tls.TLSClientSettings
import kotlinx.coroutines.*

class MqttService(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var client: MQTTClient? = null
    private var runner: Job? = null

    fun connect(
        host: String,
        port: Int = 1883,
        useTls: Boolean = false,
        clientId: String? = null,
        username: String? = null,
        password: String? = null
    ) {
        val tls = if (useTls) TLSClientSettings(serverCertificate = null) else null

        client = MQTTClient(
            mqttVersion = MQTTVersion.MQTT5,
            address = host,
            port = port,
            tls = tls,
            onConnected = { /* TODO: notify */ },
            onDisconnected = { /* TODO: notify */ },
            publishReceived = { /* 受信メッセージ処理 */ }
        )

        runner = scope.launch { client?.run() }
    }

    fun subscribe(topic: String, qos: Qos = Qos.AT_MOST_ONCE) {
        client?.subscribe(listOf(Subscription(topic, SubscriptionOptions(qos))))
    }

    fun publish(topic: String, payload: ByteArray, qos: Qos = Qos.AT_MOST_ONCE, retain: Boolean = false) {
        client?.publish(retain, qos, topic, payload.toUByteArray())
    }

    fun stop() {
        runner?.cancel()
    }

    // どこか共通の場所に追加（commonMain）
    fun publishText(topic: String, text: String) {
        // Kotlin/MPPで共通に使えるのは encodeToByteArray()
        publish(topic, text.encodeToByteArray())
    }

}
