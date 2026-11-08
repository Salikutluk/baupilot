import SwiftUI
import UIKit

/// Natural size of a component laid out at a fixed width. For components only, never `ScreenScaffold`;
/// greedy views report the proposal.
@MainActor
func size(of view: some View, width: CGFloat) -> CGSize {
    let hosting = UIHostingController(
        rootView: view.frame(width: width).fixedSize(horizontal: false, vertical: true))
    return hosting.sizeThatFits(in: CGSize(width: width, height: .greatestFiniteMagnitude))
}
