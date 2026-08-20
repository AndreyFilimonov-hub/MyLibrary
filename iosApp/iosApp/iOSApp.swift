import SwiftUI
import Shared

@main
struct iOSApp: App {

    init() {
        KoinIosKt().initKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
