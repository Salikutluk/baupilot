#if DEBUG
import SwiftUI

/// Debug-only screen stacking every gallery page under a page title.
struct GalleryScreen: View {
    var body: some View {
        ScrollView(.vertical) {
            VStack(alignment: .leading, spacing: 0) {
                ForEach(GalleryPage.allCases, id: \.self) { page in
                    Text(page.goldenName)
                        .font(.inter(17, .bold))
                        .foregroundStyle(Tokens.text.color)
                        .padding(.horizontal, 20)
                        .padding(.top, 24)
                    page.content
                }
            }
        }
        .background(Tokens.bg.color)
        .foregroundStyle(Tokens.text.color)
    }
}
#endif
