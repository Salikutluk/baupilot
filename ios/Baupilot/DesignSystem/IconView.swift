import SwiftUI

/// Draws a generated [BaupilotIcon] as a tinted template image. Icons are decorative, so the
/// view is hidden from accessibility.
public struct Icon: View {
    private let icon: BaupilotIcon
    private let size: CGFloat
    private let color: Color

    public init(_ icon: BaupilotIcon, size: CGFloat, color: Color) {
        self.icon = icon
        self.size = size
        self.color = color
    }

    public var body: some View {
        Image(icon.rawValue)
            .resizable()
            .renderingMode(.template)
            .frame(width: size, height: size)
            .foregroundStyle(color)
            .accessibilityHidden(true)
    }
}
