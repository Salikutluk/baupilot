import SwiftUI

@main
struct BaupilotApp: App {
    init() {
        BaupilotFonts.registerIfNeeded()
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
