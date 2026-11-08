import SwiftUI

struct RootView: View {
    @State private var navigation = AppNavigation()
    @State private var data: AppData?

    var body: some View {
        ZStack {
            Tokens.bg.color.ignoresSafeArea()
            if let data {
                AppContent(navigation: $navigation, data: data)
            }
        }
        .task {
            do {
                data = try await AppData.load(from: MockRepository.bundled())
            } catch {
                #if DEBUG
                assertionFailure("AppData failed to load: \(error)")
                #endif
            }
        }
    }
}
