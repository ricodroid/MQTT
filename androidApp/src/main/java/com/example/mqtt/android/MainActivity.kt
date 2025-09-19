package com.example.mqtt.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.mqtt.MqttClientAsync

class MainActivity : ComponentActivity() {

    private val client = MqttClientAsync()
    private val topic = "demo/topic"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        client.connect(
            host = "test.mosquitto.org",
            port = 1883,
            tls = false
        ) { err ->
            if (err != null) {
                Log.e("MQTT", "connect failed", err)
                return@connect
            }
            // 購読（コールバックで受信）
            client.subscribe(topic, qos = 0) { bytes ->
                Log.d("MQTT", "Android RX = ${bytes.decodeToString()}")
            }
            // 送信（完了コールバック任意）
            client.publish(topic, "hello from Android".encodeToByteArray()) { pubErr ->
                if (pubErr != null) Log.e("MQTT", "publish failed", pubErr)
            }
        }
    }

    override fun onDestroy() {
        client.disconnect() // 失敗してもコールバック握りつぶしでOK
        super.onDestroy()
    }
}
