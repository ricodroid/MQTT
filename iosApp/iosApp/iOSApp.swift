import SwiftUI
import CocoaMQTT   // 追加
// import shared   // KMMのAPIを使うなら（Shared.xcframeworkを入れた場合）

@main
struct iOSApp: App {
    private let mqtt = MqttTester()   // さっきのテスター（CocoaMQTTDelegate実装）

    var body: some Scene {
        WindowG<uses-permission android:name="android.permission.INTERNET" />roup {
            ContentView()
                .onAppear {          // アプリ表示時に接続開始
                    mqtt.start()
                }
        }
    }
}
