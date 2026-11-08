import SwiftUI

/// Rounded square holding one icon: the Login brand mark (40) and the feature bullets (32).
public struct IconTile: View {
    public enum Size {
        case brand40, feature32

        var side: CGFloat {
            switch self {
            case .brand40: return 40
            case .feature32: return 32
            }
        }

        var radius: CGFloat {
            switch self {
            case .brand40: return 11
            case .feature32: return 9
            }
        }

        var iconSize: CGFloat {
            switch self {
            case .brand40: return 22
            case .feature32: return 17
            }
        }
    }

    private let icon: BaupilotIcon
    private let size: Size
    private let background: ColorToken
    private let foreground: ColorToken

    public init(icon: BaupilotIcon, size: Size, background: ColorToken, foreground: ColorToken) {
        self.icon = icon
        self.size = size
        self.background = background
        self.foreground = foreground
    }

    public var body: some View {
        Icon(icon, size: size.iconSize, color: foreground.color)
            .frame(width: size.side, height: size.side)
            .background(
                RoundedRectangle(cornerRadius: size.radius).fill(background.color))
    }
}

/// Multi-line text that breaks like Chrome: greedily, with no last-word push-down. SwiftUI `Text`
/// moves a lone last word up ("welcher / Handwerker kommt"), `UILabel` with an empty line-break
/// strategy does not. `lineHeight` is the line box to use.
struct GreedyLabel: UIViewRepresentable {
    let text: String
    let size: CGFloat
    let weight: InterWeight
    let color: ColorToken
    let lineHeight: CGFloat

    @Environment(\.colorScheme) private var scheme

    func makeUIView(context: Context) -> UILabel {
        let label = UILabel()
        label.numberOfLines = 0
        label.lineBreakMode = .byWordWrapping
        label.setContentHuggingPriority(.defaultLow, for: .horizontal)
        label.setContentCompressionResistancePriority(.defaultLow, for: .horizontal)
        return label
    }

    /// UIKit sits the baseline of a fixed-height line at `lineHeight − descender`; CSS puts it at the rounded ascent.
    /// The difference is how far the glyphs are raised to land on Chrome's baseline.
    private var baselineRise: CGFloat {
        lineHeight - InterMetrics.descender * size / InterMetrics.unitsPerEm - InterMetrics.baselineOffset(size)
    }

    func updateUIView(_ label: UILabel, context: Context) {
        let style = NSMutableParagraphStyle()
        style.lineBreakStrategy = []
        style.minimumLineHeight = lineHeight
        style.maximumLineHeight = lineHeight
        label.attributedText = NSAttributedString(
            string: text,
            attributes: [
                .font: UIFont(name: weight.postScriptName, size: size) ?? .systemFont(ofSize: size),
                .foregroundColor: UIColor(color.color(scheme)),
                .paragraphStyle: style,
                .baselineOffset: baselineRise,
            ])
    }

    func sizeThatFits(_ proposal: ProposedViewSize, uiView: UILabel, context: Context) -> CGSize? {
        let width = proposal.width ?? .greatestFiniteMagnitude
        updateUIView(uiView, context: context)
        let fitted = uiView.sizeThatFits(CGSize(width: width, height: .greatestFiniteMagnitude))
        return CGSize(width: min(fitted.width, width), height: fitted.height)
    }
}

/// Feature bullet: a 32 pt soft accent tile and a 15/500 text, gap 12.
public struct FeatureRow: View {
    private let icon: BaupilotIcon
    private let text: String

    public init(icon: BaupilotIcon, text: String) {
        self.icon = icon
        self.text = text
    }

    public var body: some View {
        HStack(spacing: 12) {
            IconTile(
                icon: icon, size: .feature32, background: Tokens.accentSoft,
                foreground: Tokens.accent)
            GreedyLabel(text: text, size: 15, weight: .medium, color: Tokens.text, lineHeight: InterMetrics.lineBox(15))
                .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}
