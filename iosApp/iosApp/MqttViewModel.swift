//
//  MqttViewModel.swift
//  iosApp
//
//  Created by r_murata on 2025/09/19.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import shared

// ヘルパー（前に使ったやつ）
func toKotlinByteArray(_ bytes: [UInt8]) -> KotlinByteArray {
    let arr = KotlinByteArray(size: Int32(bytes.count))
    for (i, b) in bytes.enumerated() { arr.set(index: Int32(i), value: Int8(bitPattern: b)) }
    return arr
}
func toSwiftBytes(_ kbytes: KotlinByteArray) -> [UInt8] {
    let n = Int(kbytes.size)
    var out = [UInt8](repeating: 0, count: n)
    for i in 0..<n { out[i] = UInt8(bitPattern: kbytes.get(index: Int32(i))) }
    return out
}

final class MqttViewModel: ObservableObject {

    private let mqtt = ClientFactory.shared.makeController()
    private let topic = "demo/topic"

    func start() {
        // Broker もファクトリ経由（すべての引数を指定できる）
        let b = ClientFactory.shared.broker(
            // host: "127.0.0.1", // iOSエミュレーター
            host: "10.0.0.158", // iOS実機 ipconfig getifaddr en0 でipを検索して入れてる
            port: 1883,
            tls: false,
            clientId: nil,
            username: nil,
            password: nil,
            keepAliveSec: 30
        )
        mqtt.setBroker(b: b)

        mqtt.connect { err in
            if let e = err { print("connect error:", e); return }

            // 1) 先に購読（CBOR）
            CborAPI.shared.subscribeTelemetry(
                controller: self.mqtt,
                topic: "demo/telemetry",
                qos: 0
            ) { tel in
                print("iOS CBOR購読 telemetry =", tel)
            }

            // 2) その後に送信（CBOR）
            let tel = Telemetry(temp: 25,
                                ts: Int64(Date().timeIntervalSince1970 * 1000),
                                deviceId: "ios")

            CborAPI.shared.publishTelemetry(
                controller: self.mqtt,
                topic: "demo/telemetry",
                obj: tel,
                qos: 0,
                retain: false
            ) { e in
                if let e = e { print("publish error:", e) }
            }

            // テキスト送信
            self.mqtt.publishText(topic: self.topic, text: "[ios] hello") { e in
                if let e = e { print("publish error:", e) }
            }
        }

    }
    
    func publish(text: String) {
        mqtt.publishText(topic: topic, text: text) // ← ここがSwift→Kotlinの入口
    }

    func stop() {
        mqtt.disconnect { _ in }
    }
}
