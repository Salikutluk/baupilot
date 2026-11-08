import SwiftUI

/// Initials in an accent circle (Profil card, IosProfil:24).
public struct Avatar: View {
    public enum Size {
        case s48

        var side: CGFloat { 48 }
        var fontSize: CGFloat { 17 }
    }

    private let initials: String
    private let size: Size

    public init(initials: String, size: Size) {
        self.initials = initials
        self.size = size
    }

    public var body: some View {
        Text(initials)
            .interText(size.fontSize, .bold)
            .foregroundStyle(Tokens.onAccent.color)
            .lineLimit(1)
            .frame(width: size.side, height: size.side)
            .background(Tokens.accent.color, in: Circle())
            .accessibilityHidden(true)
    }
}
