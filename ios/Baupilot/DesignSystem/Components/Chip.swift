import SwiftUI

/// Larger tag chip (13/600) used in rows of tags; shares the status tones.
public struct Chip: View {
    private let text: String
    private let tone: StatusBadge.Tone

    public init(_ text: String, tone: StatusBadge.Tone) {
        self.text = text
        self.tone = tone
    }

    public var body: some View {
        Text(text)
            .font(.inter(13, .semibold))
            .foregroundStyle(tone.foreground.color)
            .lineLimit(1)
            .fixedSize()
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(tone.background.color, in: RoundedRectangle(cornerRadius: 8))
    }
}
