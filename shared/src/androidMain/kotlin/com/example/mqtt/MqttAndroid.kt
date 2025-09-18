package com.example.mqtt

import com.hivemq.client.mqtt.MqttClient as HmClient
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer


private var client: Mqtt5AsyncClient? = null  // ★型を明示

actual object MqttClientFactory {
    actual fun create(): MqttClient = AndroidMqttClient()
}

private class AndroidMqttClient : MqttClient {
    private var client: Mqtt5AsyncClient? = null

    override suspend fun connect(
        host: String,
        port: Int,
        tls: Boolean,
        clientId: String?,
        username: String?,
        password: String?,
        keepAliveSec: Int
    ) {
        withContext(Dispatchers.IO) {
            val b = HmClient.builder()
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(port)

            if (clientId != null) b.identifier(clientId)
            if (tls) b.sslWithDefaultConfig()

            // ★ ここが重要：最初から Mqtt5AsyncClient にする
            val async = b.buildAsync()

            val connect = Mqtt5Connect.builder()
                .cleanStart(true)
                .keepAlive(keepAliveSec)
                .apply {
                    if (username != null) {
                        simpleAuth()
                            .username(username)
                            .apply { if (password != null) password(password.toByteArray()) }
                            .applySimpleAuth()
                    }
                }
                .build()

            async.connect(connect).get()
            client = async
        }
    }

    override fun subscribe(topic: String, qos: Int, onMessage: (ByteArray) -> Unit) {
        val q = when (qos) {
            2 -> MqttQos.EXACTLY_ONCE
            1 -> MqttQos.AT_LEAST_ONCE
            else -> MqttQos.AT_MOST_ONCE
        }

        client?.subscribeWith()
            ?.topicFilter(topic)
            ?.qos(q)
            ?.callback { msg ->
                val bytes = msg.payload.orElse(null)?.toByteArray() ?: ByteArray(0)
                onMessage(bytes)
            }
            ?.send()

    }


    override suspend fun publish(topic: String, payload: ByteArray, qos: Int, retain: Boolean) {
        withContext(Dispatchers.IO) {
            val q = when (qos) {
                2 -> MqttQos.EXACTLY_ONCE
                1 -> MqttQos.AT_LEAST_ONCE
                else -> MqttQos.AT_MOST_ONCE
            }
            client?.publishWith()
                ?.topic(topic)
                ?.payload(payload)
                ?.qos(q)
                ?.retain(retain)
                ?.send()
                ?.get()
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            client?.disconnect()?.get()
            client = null
        }
    }



    // ByteBuffer → ByteArray 変換（position を汚さないよう read-only コピーで）
    private fun ByteBuffer.toByteArray(): ByteArray {
        val dup = this.asReadOnlyBuffer()
        val out = ByteArray(dup.remaining())
        dup.get(out)
        return out
    }
}