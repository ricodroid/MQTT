package com.example.mqtt

import MQTTClient
import kotlinx.coroutines.*
import mqtt.MQTTVersion
import mqtt.Subscription
import mqtt.packets.Qos
import mqtt.packets.mqttv5.ReasonCode
import mqtt.packets.mqttv5.SubscriptionOptions
import socket.tls.TLSClientSettings

class KmpMqttClient(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : MqttClient {

    @Suppress("unused")
    constructor() : this(CoroutineScope(Dispatchers.Default))

    private var client: MQTTClient? = null
    private var runner: Job? = null
    private var onMsg: ((ByteArray) -> Unit)? = null

    override suspend fun connect(
        host: String,
        port: Int,
        tls: Boolean,
        clientId: String?,
        username: String?,          // 0.4.8 では username は未使用（パラメータ無し）
        password: String?,          // 0.4.8 では password は UByteArray? 型
        keepAliveSec: Int
    ) = withContext(Dispatchers.Default) {
        val tlsCfg = if (tls) TLSClientSettings(serverCertificate = null) else null

        client = MQTTClient(
            mqttVersion = MQTTVersion.MQTT5,
            address = host,
            port = port,
            tls = tlsCfg,
            clientId = clientId,
            // 0.4.8: username パラメータは無し
            password = password?.encodeToByteArray()?.toUByteArray(), // ← 型をUByteArray?に
            keepAlive = keepAliveSec,                                  // ← Int をそのまま
            onConnected = { _ /* reasonCode */ -> /* optional */ },
            onDisconnected = { _ /* reasonCode */ -> /* optional */ },
            publishReceived = { pkt ->
                println("KMP[shared] bytes=${pkt.payload?.size ?: 0}") // ← ★共通コードの証拠ログ
                val bytes = pkt.payload?.toByteArray() ?: byteArrayOf()
                onMsg?.invoke(bytes)
            }
        )

        // ブロッキング run ループをバックグラウンドで開始
        runner = scope.launch { client?.run() }
    }

    override fun subscribe(topic: String, qos: Int, onMessage: (ByteArray) -> Unit) {
        onMsg = onMessage
        val q = when (qos) {
            2 -> Qos.EXACTLY_ONCE
            1 -> Qos.AT_LEAST_ONCE
            else -> Qos.AT_MOST_ONCE
        }
        // 0.4.8 でもこの形で入る構成が多いです。もしエラーなら下の代替へ。
        client?.subscribe(listOf(Subscription(topic, SubscriptionOptions(q))))
        // 代替（必要なら↑をコメントアウトして↓を使う）
        // client?.subscribe(listOf(Subscription(topic, q)))
    }

    override suspend fun publish(topic: String, payload: ByteArray, qos: Int, retain: Boolean) {
        val q = when (qos) {
            2 -> Qos.EXACTLY_ONCE
            1 -> Qos.AT_LEAST_ONCE
            else -> Qos.AT_MOST_ONCE
        }
        withContext(Dispatchers.Default) {
            client?.publish(retain, q, topic, payload.toUByteArray())
        }
    }

    override suspend fun disconnect() {
        runner?.cancel()
        client?.disconnect(ReasonCode.SUCCESS)   // SUCCESS=0x00
        client = null
        onMsg = null
    }

}