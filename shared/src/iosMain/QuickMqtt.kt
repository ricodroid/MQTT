import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.Subscription
import io.github.davidepianca98.mqtt.packets.Qos
import io.github.davidepianca98.mqtt.packets.mqttv5.SubscriptionOptions
// TLS を使うときだけ
import io.github.davidepianca98.socket.tls.TLSClientSettings
// Coroutines
import kotlinx.coroutines.*


fun quickMqttSmokeTest() = runBlocking {
    val client = MQTTClient(
        MQTTVersion.MQTT5,
        "test.mosquitto.org", // 公開ブローカー
        1883,                 // 平文(TCP)。TLSなら 8883
        null                  // TLS: TLSClientSettings("mosquitto.org.crt") など
    ) { packet ->
        println(packet.payload?.toByteArray()?.decodeToString())
    }

    client.subscribe(listOf(Subscription("demo/kmp/#", SubscriptionOptions(Qos.AT_LEAST_ONCE))))
    client.publish(false, Qos.AT_LEAST_ONCE, "demo/kmp/chat", "hello from KMP".encodeToByteArray().toUByteArray())
    client.run() // 簡易ブロッキング（UIから使うなら別スレッド/コルーチンに）
}
