package com.example.mqtt

import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor

/**
 * KMP一本化：接続/購読/送信/状態を共通Kotlinで完結
 */
class MqttController(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    // 実体は非suspendラッパー（Swift/Android両対応）
    private val client = MqttClientAsync(client = KmpMqttClient(), scope = scope)

    @PublishedApi
    internal fun publishBytes(
        topic: String,
        payload: ByteArray,
        qos: Int = 0,
        retain: Boolean = false,
        onComplete: (Throwable?) -> Unit = {}
    ) { client.publish(topic, payload, qos, retain, onComplete) }

    // ★ これを追加
    @PublishedApi
    internal fun subscribeBytes(
        topic: String,
        qos: Int = 0,
        onBytes: (ByteArray) -> Unit
    ) {
        client.subscribe(topic, qos) { bytes ->
            // ログ用に messages へも載せたい場合はプレースホルダ文字列にして追加
            _messages.value = _messages.value + Message(topic, "<binary>", bytes)
            onBytes(bytes)
        }
    }

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

    // 追加（Swiftから引数省略で呼べるように）
    @Suppress("unused")
    fun publishText(topic: String, text: String) =
        publishText(topic, text, 0, false) {}

    /** 完了コールバックだけ受ける版も */
    @Suppress("unused")
    fun publishText(topic: String, text: String, onComplete: (Throwable?) -> Unit) =
        publishText(topic, text, 0, false, onComplete)

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

@PublishedApi
internal val CborBin = Cbor {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
/** Kotlin側（Androidなど）で使う：reified 版 */
inline fun <reified T> MqttController.subscribeCbor(
    topic: String,
    qos: Int = 0,
    crossinline onObject: (T) -> Unit
) {
    subscribeBytes(topic, qos) { bytes ->   // ← ここを subscribeBytes に
        runCatching { CborBin.decodeFromByteArray<T>(bytes) }
            .onSuccess(onObject)
            .onFailure { /* ログ */ }
    }
}

/** Kotlin側（Androidなど）で使う：reified 版 */
inline fun <reified T> MqttController.publishCbor(
    topic: String,
    obj: T,
    qos: Int = 0,
    retain: Boolean = false,
    noinline onComplete: (Throwable?) -> Unit = {}
) {
    val payload = CborBin.encodeToByteArray(obj)
    publishBytes(topic, payload, qos, retain, onComplete)
}

/** Swift(iOS)からも使える serializer 指定版（reified不可のため） */
fun <T> MqttController.subscribeCbor(
    topic: String,
    kSerializer: KSerializer<T>,
    qos: Int = 0,
    onObject: (T) -> Unit
) {
    subscribeBytes(topic, qos) { bytes ->
        runCatching { CborBin.decodeFromByteArray(kSerializer, bytes) }
            .onSuccess(onObject)
            .onFailure { /* ログ */ }
    }
}

/** Swift(iOS)からも使える serializer 指定版 */
fun <T> MqttController.publishCbor(
    topic: String,
    obj: T,
    serializer: KSerializer<T>,
    qos: Int = 0,
    retain: Boolean = false,
    onComplete: (Throwable?) -> Unit = {}
) {
    val payload = CborBin.encodeToByteArray(serializer, obj)
    publishBytes(topic, payload, qos, retain, onComplete)
}