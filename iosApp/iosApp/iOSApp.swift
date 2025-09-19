import SwiftUI
import shared   // ← KMM モジュール

@main
struct iOSApp: App {
    @StateObject private var vm = MqttViewModel()
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onAppear { vm.start() }
                .onDisappear { vm.stop() }
        }
    }
}
