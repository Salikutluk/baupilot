import SwiftUI

/// The app's own content view and the snapshot entry point: Login when logged out, else the tab shell.
struct AppContent: View {
    @Binding var navigation: AppNavigation
    let data: AppData

    var body: some View {
        if navigation.isLoggedIn {
            AppShell(navigation: $navigation, data: data)
        } else {
            LoginScreen(model: data.login, onSubmit: { navigation.logIn() })
        }
    }
}
