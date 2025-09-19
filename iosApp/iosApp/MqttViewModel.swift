//
//  MqttViewModel.swift
//  iosApp
//
//  Created by r_murata on 2025/09/19.
//  Copyright © 2025 orgName. All rights reserved.
//
import SwiftUI
import shared

func toKotlinByteArray(_ bytes: [UInt8]) -> KotlinByteArray {
    let arr = KotlinByteArray(size: Int32(bytes.count))
    for (i, b) in bytes.enumerated() {
        arr.set(index: Int32(i), value: Int8(bitPattern: b))
    }
    return arr
}

// KotlinByteArray → [UInt8]（受信時の変換にも便利）
func toSwiftBytes(_ kbytes: KotlinByteArray) -> [UInt8] {
    let n = Int(kbytes.size)
    var out = [UInt8](repeating: 0, count: n)
    for i in 0..<n {
        out[i] = UInt8(bitPattern: kbytes.get(index: Int32(i)))
    }
    return out
}

final class MqttViewModel: ObservableObject {
    private let client = ClientFactory.shared.makeKmpMqttClient()   // ← ここ！

    func start() {
        client.connect(
            host: "test.mosquitto.org",
            port: 1883,
            tls: false,
            clientId: nil,
            username: nil,
            password: nil,
            keepAliveSec: 30
        ) { error in
            if let e = error { print("connect error:", e); return }

            self.client.subscribe(topic: "demo/topic", qos: 0) { bytes in
                // KotlinByteArray → [UInt8] → String
                var buf = [UInt8](repeating: 0, count: Int(bytes.size))
                for i in 0..<buf.count { buf[i] = UInt8(truncatingIfNeeded: bytes.get(index: Int32(i))) }
                let text = String(bytes: buf, encoding: .utf8) ?? "<non-utf8>"
                print("iOS RX =", text)
            }

            let payloadSwift = Array("hello from iOS".utf8)          // [UInt8]
            let payload = toKotlinByteArray(payloadSwift)            // KotlinByteArray に変換
            self.client.publish(topic: "demo/topic", payload: payload, qos: 0, retain: false) { e in
                if let e = e { print("publish error:", e) }
            }
        }
    }

    func stop() {
        client.disconnect { _ in }
    }
}
