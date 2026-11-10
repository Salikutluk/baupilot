import SwiftUI

/// Coloured link whose text wraps greedily like Chrome (IosEsEbenheit:62): leading aligned, at least `minHeight` tall
/// with the text centred, no negative margin. A nil `action` renders it and does nothing. `InlineLink` is the
/// accent, single-line variant.
public struct TextLink: View {
    private let title: String
    private let color: ColorToken
    private let size: CGFloat
    private let weight: InterWeight
    private let minHeight: CGFloat
    private let action: (() -> Void)?

    public init(
        _ title: String, color: ColorToken, size: CGFloat = 15, weight: InterWeight = .semibold,
        minHeight: CGFloat = 44, action: (() -> Void)?
    ) {
        self.title = title
        self.color = color
        self.size = size
        self.weight = weight
        self.minHeight = minHeight
        self.action = action
    }

    public var body: some View {
        Button(action: action ?? {}) {
            GreedyLabel(text: title, size: size, weight: weight, color: color, lineHeight: .normal)
                .frame(maxWidth: .infinity, minHeight: minHeight, alignment: .leading)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}
