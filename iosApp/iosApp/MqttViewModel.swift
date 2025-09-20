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
    // 旧: MqttController() は使わない（init() unavailable の回避）
    private let mqtt = ClientFactory.shared.makeController()
    private let topic = "demo/topic"

    func start() {
        // Broker もファクトリ経由（すべての引数を指定できる）
        let b = ClientFactory.shared.broker(
            host: "127.0.0.1",
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

            self.mqtt.subscribe(topic: self.topic, qos: 0) { text in
                print("iOS RX =", text)
            }

            self.mqtt.publishText(topic: self.topic, text: "[ios] hello", qos: 0, retain: false) { e in
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
