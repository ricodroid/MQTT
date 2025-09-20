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
    // suspend を直接呼ばない Async ラッパーを使う
    private let client = ClientFactory.shared.makeAsyncMqttClient()
    private let topic = "demo/topic"

    func start() {
        // IPv6問題回避：数値IPv4を使用（あなたの dig 結果）
        client.connect(host: "5.196.78.28", port: 1883, tls: false,
                       clientId: nil, username: nil, password: nil, keepAliveSec: 30) { err in
            if let e = err { print("connect error:", e); return }

            self.client.subscribe(topic: self.topic, qos: 0) { kbytes in
                let text = String(bytes: toSwiftBytes(kbytes), encoding: .utf8) ?? "<non-utf8>"
                print("iOS RX =", text)
            }

            let payload = toKotlinByteArray(Array("[ios] hello".utf8))
            self.client.publish(topic: self.topic, payload: payload, qos: 0, retain: false) { pubErr in
                if let e = pubErr { print("publish error:", e) }
            }
        }
    }

    func stop() {
        client.disconnect { _ in }
    }
}
