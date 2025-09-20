package com.example.mqtt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


object ClientFactory {
    @Suppress("unused")
    fun makeKmpMqttClient(): KmpMqttClient =
        KmpMqttClient(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default))

    // ★ 追加：iOS/Swift から使う非suspendラッパー
    @Suppress("unused")
    fun makeAsyncMqttClient(): MqttClientAsync =
        MqttClientAsync()
}