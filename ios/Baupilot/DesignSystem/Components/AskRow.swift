import SwiftUI

/// Rotated square marker of the schedule: 10 pt in the ask rows, 8 pt in the legend and the chart.
/// The optional outline is a 2 pt ring drawn outside the square (CSS `outline`, IosZeitplan:62).
public struct DiamondMarker: View {
    public enum Size {
        case s10, s8

        var side: CGFloat { self == .s10 ? 10 : 8 }
    }

    private static let outlineWidth: CGFloat = 2

    private let size: Size
    private let outline: ColorToken?

    public init(size: Size, outline: ColorToken? = nil) {
        self.size = size
        self.outline = outline
    }

    public var body: some View {
        ZStack {
            if let outline {
                Rectangle()
                    .fill(outline.color)
                    .frame(width: size.side + 2 * Self.outlineWidth, height: size.side + 2 * Self.outlineWidth)
            }
            Rectangle()
                .fill(Tokens.accent.color)
                .frame(width: size.side, height: size.side)
        }
        .rotationEffect(.degrees(45))
        .frame(width: size.side, height: size.side)
        .accessibilityHidden(true)
    }
}

/// "Jetzt Handwerker anfragen" row: diamond, title with subtitle and a chevron. A nil action renders
/// the row and does nothing.
public struct AskRow: View {
    private enum Metrics {
        static let minHeight: CGFloat = 44
        static let gap: CGFloat = 10
        static let textGap: CGFloat = 1
        static let chevron: CGFloat = 14
    }

    private let title: String
    private let subtitle: String
    private let action: (() -> Void)?

    public init(title: String, subtitle: String, action: (() -> Void)?) {
        self.title = title
        self.subtitle = subtitle
        self.action = action
    }

    private var row: some View {
        HStack(spacing: Metrics.gap) {
            DiamondMarker(size: .s10)
            VStack(alignment: .leading, spacing: Metrics.textGap) {
                Text(title)
                    .interText(15, .semibold)
                    .foregroundStyle(Tokens.text.color)
                Text(subtitle)
                    .interText(13, .regular)
                    .foregroundStyle(Tokens.muted.color)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Icon(.chevronRightW24, size: Metrics.chevron, color: Tokens.muted.color)
        }
        .frame(maxWidth: .infinity, minHeight: Metrics.minHeight, alignment: .leading)
        .contentShape(Rectangle())
    }

    public var body: some View {
        if let action {
            Button(action: action) { row }.buttonStyle(.plain)
        } else {
            row
        }
    }
}
