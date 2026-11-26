import SwiftUI

/// Hit shape for a control that draws smaller than 44 × 44 pt: the drawn box grown, centred, to at least 44 in each
/// direction. It changes no layout; a box that is already 44 wide or tall keeps that side.
struct MinimumHitShape: Shape {
    static let minimum: CGFloat = 44

    func path(in rect: CGRect) -> Path {
        let width = max(rect.width, Self.minimum)
        let height = max(rect.height, Self.minimum)
        return Path(CGRect(x: rect.midX - width / 2, y: rect.midY - height / 2, width: width, height: height))
    }
}
