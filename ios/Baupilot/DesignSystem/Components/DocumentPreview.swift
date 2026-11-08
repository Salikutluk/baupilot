import SwiftUI

/// A 250 x 320 page of a document: letterhead, title block, position lines and a pinned total.
/// Always white paper, also in dark mode.
public struct DocumentPreview: View {
    public struct Line {
        public let pos: String
        public let text: String
        public let sum: String

        public init(pos: String, text: String, sum: String) {
            self.pos = pos
            self.text = text
            self.sum = sum
        }
    }

    private enum Metrics {
        static let width: CGFloat = 250
        static let height: CGFloat = 320
        static let gap: CGFloat = 9
        static var lineHeight: LineHeight { .factor(1.35) }
    }

    private let issuerName: String
    private let issuerAddress: String
    private let number: String
    private let reference: String
    private let lines: [Line]
    private let totalLabel: String
    private let total: String
    private let accessibilityLabel: String
    private let radius: CGFloat

    public init(
        issuerName: String, issuerAddress: String, number: String, reference: String, lines: [Line],
        totalLabel: String, total: String, accessibilityLabel: String, radius: CGFloat = 3
    ) {
        self.issuerName = issuerName
        self.issuerAddress = issuerAddress
        self.number = number
        self.reference = reference
        self.lines = lines
        self.totalLabel = totalLabel
        self.total = total
        self.accessibilityLabel = accessibilityLabel
        self.radius = radius
    }

    private func text(_ string: String, _ size: CGFloat, _ weight: InterWeight, _ color: ColorToken) -> some View {
        Text(string)
            .interText(size, weight, lineHeight: Metrics.lineHeight, lines: .single)
            .foregroundStyle(color.color)
    }

    private func rule(_ height: CGFloat, _ color: ColorToken) -> some View {
        Rectangle().fill(color.color).frame(height: height)
    }

    private var letterhead: some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading, spacing: 1) {
                text(issuerName, 10, .bold, Tokens.docBrand)
                text(issuerAddress, 7, .regular, Tokens.docMeta)
            }
            Spacer(minLength: 0)
            RoundedRectangle(cornerRadius: 4).fill(Tokens.docBrand.color).frame(width: 22, height: 22)
        }
    }

    private var titleBlock: some View {
        VStack(alignment: .leading, spacing: 1) {
            text(number, 9, .bold, Tokens.docInk)
            text(reference, 7, .regular, Tokens.docMeta)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.top, 4)
    }

    private func row(_ line: Line) -> some View {
        VStack(spacing: 0) {
            HStack(alignment: .top, spacing: 6) {
                text(line.pos, 7, .regular, Tokens.docPos).frame(width: 12, alignment: .leading)
                text(line.text, 7, .regular, Tokens.docInk).frame(maxWidth: .infinity, alignment: .leading)
                text(line.sum, 7, .semibold, Tokens.docInk).fixedSize()
            }
            .padding(.vertical, 4)
            rule(1, Tokens.docRuleLight)
        }
    }

    private var lineList: some View {
        VStack(spacing: 0) {
            rule(1, Tokens.docRule)
            ForEach(lines.indices, id: \.self) { row(lines[$0]) }
        }
    }

    private var totalRow: some View {
        VStack(spacing: 0) {
            rule(2, Tokens.docInk)
            HStack {
                text(totalLabel, 9, .bold, Tokens.docInk)
                Spacer(minLength: 0)
                text(total, 9, .bold, Tokens.docInk).fixedSize()
            }
            .padding(.top, 6)
        }
    }

    public var body: some View {
        VStack(alignment: .leading, spacing: Metrics.gap) {
            letterhead
            titleBlock
            lineList
            Spacer(minLength: 0)
            totalRow
        }
        .padding(EdgeInsets(top: 18, leading: 18, bottom: 14, trailing: 18))
        .frame(width: Metrics.width, height: Metrics.height, alignment: .topLeading)
        .background {
            // The shadow belongs to the paper only; on the whole view the text would cast one too.
            // CSS blur 14 / 2, the M1 convention.
            RoundedRectangle(cornerRadius: radius)
                .fill(Tokens.docPaper.color)
                .shadow(color: Tokens.docShadow.color, radius: 7, x: 0, y: 4)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityLabel)
        .accessibilityAddTraits(.isImage)
    }
}
