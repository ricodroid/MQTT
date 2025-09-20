import SwiftUI
import shared

struct ContentView: View {
    @StateObject var vm = MqttViewModel()
    @State private var text = ""

    var body: some View {
        VStack {
            TextField("message", text: $text).textFieldStyle(.roundedBorder)
            Button("Send") {
                vm.publish(text: text)   // ← publishText をここで呼ぶ
                text = ""
            }
        }
        .onAppear { vm.start() }
        .onDisappear { vm.stop() }
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
