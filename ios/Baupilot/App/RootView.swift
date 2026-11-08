import SwiftUI

struct RootView: View {
    var body: some View {
        ZStack {
            Tokens.bg.color.ignoresSafeArea()
            Text("Baupilot")
                .font(.custom("Inter-Bold", size: 24))
                .foregroundColor(Tokens.text.color)
        }
    }
}
