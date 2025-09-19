package com.example.mqtt

import kotlinx.coroutines.delay
import platform.Foundation.NSLog

actual object MqttClientFactory {
    actual fun create(): MqttClient = IosStubMqttClient()
}

/**
 * ひとまずビルドを通すための NO-OP 実装。
 * 後で CocoaMQTT(Swift) と橋渡しするまでの仮置き。
 */
private class IosStubMqttClient : MqttClient {
    override suspend fun connect(
        host: String,
        port: Int,
        tls: Boolean,
        clientId: String?,
        username: String?,
        password: String?,
        keepAliveSec: Int
    ) {
        NSLog("IosStubMqttClient.connect host=%@ port=%d tls=%@", host, port, tls.toString())
        delay(10) // 形だけのサスペンド
    }

    override fun subscribe(topic: String, qos: Int, onMessage: (ByteArray) -> Unit) {
        NSLog("IosStubMqttClient.subscribe topic=%@ qos=%d (NO-OP)", topic, qos)
        // 実装するまでは何もしない（必要ならここで onMessage を保存して、publish 時に loopback することも可）
    }

    override suspend fun publish(topic: String, payload: ByteArray, qos: Int, retain: Boolean) {
        NSLog("IosStubMqttClient.publish topic=%@ bytes=%d (NO-OP)", topic, payload.size.toLong())
        delay(10)
    }

    override suspend fun disconnect() {
        NSLog("IosStubMqttClient.disconnect (NO-OP)")
        delay(10)
    }
}