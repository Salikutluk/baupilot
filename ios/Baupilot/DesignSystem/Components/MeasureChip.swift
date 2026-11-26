import SwiftUI

/// Read-only measurement chip (IosEsEbenheit:58): 36 tall, padding 0 12, r10, 14/600, hugging its text. The label sits a third of a
/// point below the centre, where Chrome's flex centring snaps the 17 pt line box in the 36 pt chip.
public struct MeasureChip: View {
    private let text: String
    private let background: ColorToken
    private let foreground: ColorToken
    private let identifier: String?

    /// `identifier` goes on the label text, not on the chip: an identifier on the chip would be one text node with no
    /// font the accessibility audit can check (Dynamic Type finding).
    public init(_ text: String, background: ColorToken, foreground: ColorToken, identifier: String? = nil) {
        self.text = text
        self.background = background
        self.foreground = foreground
        self.identifier = identifier
    }

    public var body: some View {
        Text(text)
            .interText(14, .semibold)
            .foregroundStyle(foreground.color)
            .optionalIdentifier(identifier)
            .offset(y: 1.0 / 3)
            .padding(.horizontal, 12)
            .frame(height: 36)
            .background(RoundedRectangle(cornerRadius: 10).fill(background.color))
            .fixedSize()
    }
}
