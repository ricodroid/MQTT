package com.example.mqtt.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.mqtt.MqttClientAsync
import com.example.mqtt.MqttController

class MainActivity : ComponentActivity() {

    private val client = MqttClientAsync()
    private val topic = "demo/topic"
    private val mqtt = MqttController()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mqtt.setBroker(MqttController.Broker(host = "10.0.2.2", port = 1883, tls = false))
        mqtt.connect { err ->
            if (err != null) { Log.e("MQTT", "connect error", err); return@connect }

            mqtt.subscribe("demo/topic") { text ->
                Log.d("MQTT", "Android RX = $text")
            }

            mqtt.publishText("demo/topic", "[android] hello")
            // 例：JSON送信
            // mqtt.publishJson("demo/telemetry", mapOf("temp" to 23, "ts" to System.currentTimeMillis()))
        }
    }

    override fun onDestroy() {
        client.disconnect() // 失敗してもコールバック握りつぶしでOK
        super.onDestroy()
    }
}
