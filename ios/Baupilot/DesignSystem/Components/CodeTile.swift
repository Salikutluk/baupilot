import SwiftUI

/// Small status tile with a centred 13/700 code (IosRohbauMasse:58): 40 tall, r10. The screen lays the tiles out
/// in columns; the tile fills the width it is given.
public struct CodeTile: View {
    private let text: String
    private let background: ColorToken
    private let foreground: ColorToken

    public init(_ text: String, background: ColorToken, foreground: ColorToken) {
        self.text = text
        self.background = background
        self.foreground = foreground
    }

    public var body: some View {
        Text(text)
            .interText(13, .bold)
            .foregroundStyle(foreground.color)
            .frame(maxWidth: .infinity)
            .frame(height: 40)
            .background(RoundedRectangle(cornerRadius: 10).fill(background.color))
    }
}
