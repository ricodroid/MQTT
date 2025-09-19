package com.example.mqtt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun makeKmpMqttClient(): KmpMqttClient =
    KmpMqttClient(CoroutineScope(Dispatchers.Default))