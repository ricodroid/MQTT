package com.example.mqtt


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

// 参照がないように見えるが、iOSのプロジェクトに組み込んでいるため必要。
object ClientFactory {
    @Suppress("unused")
    fun makeController(): MqttController =
        MqttController(CoroutineScope(Dispatchers.Default))

    @Suppress("unused")
    fun broker(
        host: String,
        port: Int = 1883,
        tls: Boolean = false,
        clientId: String? = null,
        username: String? = null,
        password: String? = null,
        keepAliveSec: Int = 30,
    ): MqttController.Broker = MqttController.Broker(
        host = host,
        port = port,
        tls = tls,
        clientId = clientId,
        username = username,
        password = password,
        keepAliveSec = keepAliveSec
    )
}