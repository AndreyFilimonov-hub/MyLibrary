import SwiftUI
import Shared

@main
struct iOSApp: App {

    init() {
        KoinHelper().start()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
