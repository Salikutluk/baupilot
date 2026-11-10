import SwiftUI

/// The line above a raster (IosFlBad:48): a 12/600 `color` text at the left and, at the right, a 12 swatch (r2, 2 pt
/// `swatchBorder` inside) with its label, gap 6, space-between. The screen applies the negative bottom margin.
public struct LegendRow: View {
    private let leading: String
    private let swatchLabel: String
    private let swatchFill: ColorToken
    private let swatchBorder: ColorToken
    private let color: ColorToken

    public init(
        leading: String, swatchLabel: String, swatchFill: ColorToken, swatchBorder: ColorToken, color: ColorToken
    ) {
        self.leading = leading
        self.swatchLabel = swatchLabel
        self.swatchFill = swatchFill
        self.swatchBorder = swatchBorder
        self.color = color
    }

    public var body: some View {
        let shape = RoundedRectangle(cornerRadius: 2)
        HStack(spacing: 0) {
            Text(leading)
                .interText(12, .semibold)
                .foregroundStyle(color.color)
            Spacer(minLength: 0)
            HStack(spacing: 6) {
                shape.fill(swatchFill.color)
                    .overlay { shape.strokeBorder(swatchBorder.color, lineWidth: 2) }
                    .frame(width: 12, height: 12)
                Text(swatchLabel)
                    .interText(12, .semibold)
                    .foregroundStyle(color.color)
            }
        }
        .accessibilityElement(children: .combine)
    }
}
