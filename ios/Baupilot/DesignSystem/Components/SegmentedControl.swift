import SwiftUI

/// Prototype segmented control: a tinted container with a raised active segment.
public struct SegmentedControl: View {
    public enum SegmentHeight {
        case compact
        case regular
        case medium

        var points: CGFloat {
            switch self {
            case .compact: return 34
            case .regular: return 44
            case .medium: return 36
            }
        }
    }

    public enum Background {
        case chip(ColorToken = Tokens.chip.core)
        case seg

        var token: ColorToken {
            switch self {
            case .chip(let token): return token
            case .seg: return Tokens.seg
            }
        }
    }

    private let segments: [String]
    private let selection: Int
    private let height: SegmentHeight
    private let background: Background
    private let accessibilityLabel: String
    private let onSelect: (Int) -> Void

    public init(
        segments: [String],
        selection: Int,
        height: SegmentHeight = .compact,
        background: Background = .chip(),
        accessibilityLabel: String,
        onSelect: @escaping (Int) -> Void
    ) {
        self.segments = segments
        self.selection = selection
        self.height = height
        self.background = background
        self.accessibilityLabel = accessibilityLabel
        self.onSelect = onSelect
    }

    public var body: some View {
        HStack(spacing: 0) {
            ForEach(segments.indices, id: \.self) { index in
                segment(index)
            }
        }
        .padding(2)
        .background(background.token.color, in: RoundedRectangle(cornerRadius: 10))
        .accessibilityElement(children: .contain)
        .accessibilityLabel(accessibilityLabel)
    }

    private func segment(_ index: Int) -> some View {
        let active = index == selection
        // D14: the visual segment may be shorter than 44 pt; the hit area grows without changing layout.
        let hitInset = -max(0, (44 - height.points) / 2)
        return Button {
            onSelect(index)
        } label: {
            Text(segments[index])
                .font(.inter(14, active ? .semibold : .medium))
                .foregroundStyle(Tokens.text.color)
                .lineLimit(1)
                .frame(maxWidth: .infinity)
                .frame(height: height.points)
                .background {
                    if active {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Tokens.surface.color)
                            .shadow(color: Tokens.segShadow.color, radius: 1.5, x: 0, y: 1)
                    }
                }
                .contentShape(Rectangle().inset(by: hitInset))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(active ? [.isButton, .isSelected] : .isButton)
    }
}
