import SwiftUI

@main
struct BaupilotApp: App {
    init() {
        BaupilotFonts.registerIfNeeded()
    }

    var body: some Scene {
        WindowGroup {
            #if DEBUG
            if UserDefaults.standard.bool(forKey: "BaupilotGallery") {
                GalleryScreen()
            } else {
                RootView()
            }
            #else
            RootView()
            #endif
        }
    }
}
