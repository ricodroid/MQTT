package com.example.mqtt.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.mqtt.KmpMqttClient
import com.example.mqtt.MqttClient

class MainActivity : ComponentActivity() {

    private val client: MqttClient = KmpMqttClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            client.connect(host = "test.mosquitto.org", port = 1883, tls = false)
            client.subscribe("demo/topic", qos = 0) { bytes ->
                Log.d("MQTT", "Android RX = ${bytes.decodeToString()}")
            }
            client.publish("demo/topic", "hello from Android".encodeToByteArray())
        }


    }

    override fun onDestroy() {
        super.onDestroy()
    }
}