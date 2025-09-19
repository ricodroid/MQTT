package com.example.mqtt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Android側でコルーチンを使わずに呼べるようにする薄いラッパー。
 * Dispatchers.Default を使うので、androidApp側に coroutines-android は不要。
 */
class MqttClientAsync(
    private val client: MqttClient = KmpMqttClient(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    fun connect(
        host: String,
        port: Int = 1883,
        tls: Boolean = false,
        clientId: String? = null,
        username: String? = null,
        password: String? = null,
        keepAliveSec: Int = 30,
        onComplete: (Throwable?) -> Unit = {}
    ) {
        scope.launch {
            try {
                client.connect(host, port, tls, clientId, username, password, keepAliveSec)
                onComplete(null)
            } catch (t: Throwable) {
                onComplete(t)
            }
        }
    }

    fun subscribe(topic: String, qos: Int = 0, onMessage: (ByteArray) -> Unit) {
        client.subscribe(topic, qos, onMessage)
    }

    fun publish(
        topic: String,
        payload: ByteArray,
        qos: Int = 0,
        retain: Boolean = false,
        onComplete: (Throwable?) -> Unit = {}
    ) {
        scope.launch {
            try {
                client.publish(topic, payload, qos, retain)
                onComplete(null)
            } catch (t: Throwable) {
                onComplete(t)
            }
        }
    }

    fun disconnect(onComplete: (Throwable?) -> Unit = {}) {
        scope.launch {
            try {
                client.disconnect()
                onComplete(null)
            } catch (t: Throwable) {
                onComplete(t)
            }
        }
    }
}