package com.example.mqtt.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.mqtt.MqttClientFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val client = MqttClientFactory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 省略: setContent(...)

        lifecycleScope.launch {
            // 接続
            client.connect(host = "test.mosquitto.org", port = 1883, tls = false)
            // 購読
            client.subscribe("demo/topic", qos = 0) { bytes ->
                Log.d("MQTT", "Android RX = ${bytes.decodeToString()}")
            }
            // 送信（テスト）
            client.publish("demo/topic", "hello from Android".encodeToByteArray())
        }
    }

    override fun onDestroy() {
        lifecycleScope.launch { client.disconnect() }
        super.onDestroy()
    }
}