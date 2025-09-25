package com.example.mqtt.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.mqtt.MqttController

// アプリ起動時にtextをMQTT送信する
class MainActivity : ComponentActivity() {

    private val mqtt = MqttController()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // AndroidStudioエミュレーター
        // mqtt.setBroker(MqttController.Broker(host = "10.0.2.2", port = 1883, tls = false))
        // Android実機の接続先 ipconfig getifaddr en0 でipを検索して入れてる
        mqtt.setBroker(MqttController.Broker(host = "10.0.0.158", port = 1883, tls = false))
        mqtt.connect { err ->
            if (err != null) { Log.e("MQTT", "connect error", err); return@connect }

            mqtt.subscribe("demo/topic") { text ->
                Log.d("MQTT", "Android = $text")
            }

            mqtt.publishText("demo/topic", "[android] hello")
            // JSON送信の場合
            // mqtt.publishJson("demo/telemetry", mapOf("temp" to 23, "ts" to System.currentTimeMillis()))
        }
    }

    override fun onDestroy() {
        mqtt.disconnect()
        super.onDestroy()
    }
}
