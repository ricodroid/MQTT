package com.example.mqtt

object CborAPI {
    fun subscribeTelemetry(
        controller: MqttController,
        topic: String,
        qos: Int = 0,
        onObj: (Telemetry) -> Unit
    ) = controller.subscribeCbor(topic, Telemetry.serializer(), qos, onObj)

    fun publishTelemetry(
        controller: MqttController,
        topic: String,
        obj: Telemetry,
        qos: Int = 0,
        retain: Boolean = false,
        onComplete: (Throwable?) -> Unit = {}
    ) = controller.publishCbor(topic, obj, Telemetry.serializer(), qos, retain, onComplete)
}