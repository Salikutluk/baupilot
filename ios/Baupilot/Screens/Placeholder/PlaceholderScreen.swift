import SwiftUI

/// Stand-in for screens of later milestones: the title only, plus a back bar when pushed.
struct PlaceholderScreen: View {
    enum Presentation {
        case tabRoot
        case pushed(backLabel: String, onBack: () -> Void)
    }

    let title: String
    let presentation: Presentation

    var body: some View {
        switch presentation {
        case .tabRoot:
            ScreenScaffold(kind: .tabRoot) {
                LargeTitleHeader(title: title)
            }
            .accessibilityElement(children: .contain)
        case let .pushed(backLabel, onBack):
            ScreenScaffold(kind: .detailNoBar) {
                BackBar(label: backLabel, action: onBack)
                LargeTitleHeader(title: title)
            }
            .accessibilityElement(children: .contain)
        }
    }
}
