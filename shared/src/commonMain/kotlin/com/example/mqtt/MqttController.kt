package com.example.mqtt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * KMP一本化：接続/購読/送信/状態を共通Kotlinで完結
 */
class MqttController(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    // 実体は非suspendラッパー（Swift/Android両対応）
    private val client = MqttClientAsync(client = KmpMqttClient(), scope = scope)

    // 接続設定
    data class Broker(
        val host: String,
        val port: Int = 1883,
        val tls: Boolean = false,
        val clientId: String? = null,
        val username: String? = null,
        val password: String? = null,
        val keepAliveSec: Int = 30,
    )

    private var broker: Broker? = null

    // 受信メッセージを共有（UIでそのまま observe 可能）
    data class Message(val topic: String, val text: String, val bytes: ByteArray)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // 接続先を設定（まだ接続はしない）
    fun setBroker(b: Broker) { broker = b }

    // 接続（成功/失敗コールバック）
    fun connect(onComplete: (Throwable?) -> Unit = {}) {
        val b = broker ?: return onComplete(IllegalStateException("Broker not set"))
        client.connect(
            host = b.host, port = b.port, tls = b.tls,
            clientId = b.clientId, username = b.username, password = b.password,
            keepAliveSec = b.keepAliveSec
        ) { err -> onComplete(err) }
    }

    fun disconnect(onComplete: (Throwable?) -> Unit = {}) = client.disconnect(onComplete)

    // 購読：受けた内容は messages Flow にも蓄積
    fun subscribe(topic: String, qos: Int = 0, onText: ((String) -> Unit)? = null) {
        client.subscribe(topic, qos) { bytes ->
            val text = runCatching { bytes.decodeToString() }.getOrElse { "<non-utf8>" }
            _messages.value = _messages.value + Message(topic, text, bytes)
            onText?.invoke(text)
        }
    }

    // 送信（テキスト）
    fun publishText(topic: String, text: String, qos: Int = 0, retain: Boolean = false,
                    onComplete: (Throwable?) -> Unit = {}) {
        client.publish(topic, text.encodeToByteArray(), qos, retain, onComplete)
    }

    // 送信（JSONを簡易生成）
    fun publishJson(topic: String, map: Map<String, Any?>, qos: Int = 0, retain: Boolean = false,
                    onComplete: (Throwable?) -> Unit = {}) {
        val json = buildString {
            append('{')
            append(map.entries.joinToString(",") { (k, v) ->
                val value = when (v) {
                    null -> "null"
                    is Number, is Boolean -> v.toString()
                    else -> "\"" + v.toString().replace("\"", "\\\"") + "\""
                }
                "\"$k\":$value"
            })
            append('}')
        }
        publishText(topic, json, qos, retain, onComplete)
    }
}