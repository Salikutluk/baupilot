import SwiftUI

/// Label size of a text button; every size is weight 600.
public enum ButtonSize {
    case large, medium, small

    var fontSize: CGFloat {
        switch self {
        case .large: return 17
        case .medium: return 16
        case .small: return 15
        }
    }
}

private enum ButtonMetrics {
    static let height: CGFloat = 50
    static let radius: CGFloat = 14
    /// CSS `1.5px` borders render 1 pt wide (measured in the reference PNGs).
    static let outlineWidth: CGFloat = 1
    static let dash: [CGFloat] = [3, 2]
}

/// Draws the label exactly as given: no system tint, no pressed-state opacity.
private struct FlatButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
    }
}

/// A button whose `action` may be nil: it then renders identically and does nothing.
private struct FlatButton<Label: View>: View {
    let action: (() -> Void)?
    let label: Label

    init(action: (() -> Void)?, @ViewBuilder label: () -> Label) {
        self.action = action
        self.label = label()
    }

    var body: some View {
        Button(action: action ?? {}) { label }
            .buttonStyle(FlatButtonStyle())
    }
}

private struct TextButtonFace: View {
    let title: String
    let size: ButtonSize
    let icon: BaupilotIcon?
    let iconSize: CGFloat
    let foreground: ColorToken
    let background: ColorToken?
    let outline: ColorToken?
    var height: CGFloat = ButtonMetrics.height
    var iconSpacing: CGFloat = 6

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: ButtonMetrics.radius)
        HStack(spacing: iconSpacing) {
            if let icon {
                Icon(icon, size: iconSize, color: foreground.color)
            }
            Text(title)
                .interText(size.fontSize, .semibold)
                .foregroundStyle(foreground.color)
        }
        .frame(maxWidth: .infinity)
        .frame(height: height)
        .background {
            if let background {
                shape.fill(background.color)
            }
        }
        // The outline lies outside the 50 pt content box (content-box `<a>`), so it adds 2 x width.
        .padding(outline == nil ? 0 : ButtonMetrics.outlineWidth)
        .overlay {
            if let outline {
                shape.strokeBorder(outline.color, lineWidth: ButtonMetrics.outlineWidth)
            }
        }
        .contentShape(shape)
    }
}

/// Content height of a text button: `.regular` is the 50 pt default, `.tall` the 52 pt Login look.
public enum ButtonHeight {
    case regular, tall

    var points: CGFloat {
        switch self {
        case .regular: return ButtonMetrics.height
        case .tall: return 52
        }
    }
}

/// Filled accent button, 50 tall (52 with `.tall`), radius 14.
public struct PrimaryButton: View {
    private let title: String
    private let size: ButtonSize
    private let height: ButtonHeight
    private let action: (() -> Void)?

    public init(
        _ title: String, size: ButtonSize = .large, height: ButtonHeight = .regular,
        action: (() -> Void)?
    ) {
        self.title = title
        self.size = size
        self.height = height
        self.action = action
    }

    public var body: some View {
        FlatButton(action: action) {
            TextButtonFace(
                title: title, size: size, icon: nil, iconSize: 0,
                foreground: Tokens.onAccent, background: Tokens.accent, outline: nil,
                height: height.points)
        }
    }
}

/// Inverted button (fill `text`, ink `bg`) with an optional leading icon; 52 tall by default.
public struct InvertedButton: View {
    private let title: String
    private let icon: BaupilotIcon?
    private let height: ButtonHeight
    private let action: (() -> Void)?

    public init(
        _ title: String, icon: BaupilotIcon? = nil, height: ButtonHeight = .tall,
        action: (() -> Void)?
    ) {
        self.title = title
        self.icon = icon
        self.height = height
        self.action = action
    }

    public var body: some View {
        FlatButton(action: action) {
            TextButtonFace(
                title: title, size: .large, icon: icon, iconSize: 18,
                foreground: Tokens.bg, background: Tokens.text, outline: nil,
                height: height.points, iconSpacing: 8)
        }
    }
}

/// Chip-filled button, 50 tall, radius 14.
public struct SecondaryButton: View {
    private let title: String
    private let size: ButtonSize
    private let chip: ColorToken
    private let action: (() -> Void)?

    public init(
        _ title: String, size: ButtonSize = .large, chip: ColorToken = Tokens.chip.core,
        action: (() -> Void)?
    ) {
        self.title = title
        self.size = size
        self.chip = chip
        self.action = action
    }

    public var body: some View {
        FlatButton(action: action) {
            TextButtonFace(
                title: title, size: size, icon: nil, iconSize: 0,
                foreground: Tokens.text, background: chip, outline: nil)
        }
    }
}

/// Square 50 x 50 chip-filled icon button.
public struct SecondaryIconButton: View {
    private let icon: BaupilotIcon
    private let label: String
    private let chip: ColorToken
    private let action: (() -> Void)?

    public init(
        icon: BaupilotIcon = .plusW22, label: String, chip: ColorToken = Tokens.chip.core,
        action: (() -> Void)?
    ) {
        self.icon = icon
        self.label = label
        self.chip = chip
        self.action = action
    }

    public var body: some View {
        FlatButton(action: action) {
            Icon(icon, size: 22, color: Tokens.text.color)
                .frame(width: ButtonMetrics.height, height: ButtonMetrics.height)
                .background(
                    RoundedRectangle(cornerRadius: ButtonMetrics.radius)
                        .fill(chip.color))
                .contentShape(RoundedRectangle(cornerRadius: ButtonMetrics.radius))
        }
        .accessibilityLabel(label)
    }
}

/// Outlined error button ("Mangel"): 50 pt content plus an outer 1 pt border, 52 pt tall.
public struct DefectButton: View {
    private let title: String
    private let size: ButtonSize
    private let showsIcon: Bool
    private let errColor: ColorToken
    private let action: (() -> Void)?

    public init(
        _ title: String,
        size: ButtonSize = .small,
        showsIcon: Bool = false,
        errColor: ColorToken = Tokens.err.core,
        action: (() -> Void)?
    ) {
        self.title = title
        self.size = size
        self.showsIcon = showsIcon
        self.errColor = errColor
        self.action = action
    }

    public var body: some View {
        FlatButton(action: action) {
            TextButtonFace(
                title: title, size: size, icon: showsIcon ? .alertTriangleW22 : nil, iconSize: 18,
                foreground: errColor, background: nil, outline: errColor)
        }
    }
}

/// Filled ok button ("In Ordnung"), 50 tall.
public struct OkButton: View {
    private let title: String
    private let size: ButtonSize
    private let showsIcon: Bool
    private let ok: ColorToken
    private let okInk: ColorToken
    private let action: (() -> Void)?

    public init(
        _ title: String,
        size: ButtonSize = .medium,
        showsIcon: Bool = false,
        ok: ColorToken = Tokens.ok.core,
        okInk: ColorToken = Tokens.okInk.core,
        action: (() -> Void)?
    ) {
        self.title = title
        self.size = size
        self.showsIcon = showsIcon
        self.ok = ok
        self.okInk = okInk
        self.action = action
    }

    public var body: some View {
        FlatButton(action: action) {
            TextButtonFace(
                title: title, size: size, icon: showsIcon ? .checkW30 : nil, iconSize: 18,
                foreground: okInk, background: ok, outline: nil)
        }
    }
}

/// Dashed accent camera button: 50 x 50 with an accessibility label, or full width via `wide`.
public struct PhotoButton: View {
    private let label: String
    private let title: String?
    private let action: (() -> Void)?

    public init(label: String, action: (() -> Void)?) {
        self.label = label
        self.title = nil
        self.action = action
    }

    private init(wideTitle: String, action: (() -> Void)?) {
        self.label = wideTitle
        self.title = wideTitle
        self.action = action
    }

    /// Full-width variant with a 15/600 title next to a 20 pt camera icon.
    public static func wide(_ title: String, action: (() -> Void)?) -> PhotoButton {
        PhotoButton(wideTitle: title, action: action)
    }

    private var dashedBorder: some View {
        RoundedRectangle(cornerRadius: ButtonMetrics.radius)
            .strokeBorder(
                Tokens.accent.color,
                style: StrokeStyle(lineWidth: ButtonMetrics.outlineWidth, dash: ButtonMetrics.dash))
    }

    public var body: some View {
        FlatButton(action: action) {
            if let title {
                HStack(spacing: 8) {
                    Icon(.camera, size: 20, color: Tokens.accent.color)
                    Text(title)
                        .interText(15, .semibold)
                        .foregroundStyle(Tokens.accent.color)
                }
                .frame(maxWidth: .infinity)
                .frame(height: ButtonMetrics.height)
                .overlay { dashedBorder }
                .contentShape(RoundedRectangle(cornerRadius: ButtonMetrics.radius))
            } else {
                Icon(.camera, size: 22, color: Tokens.accent.color)
                    .frame(width: ButtonMetrics.height, height: ButtonMetrics.height)
                    .overlay { dashedBorder }
                    .contentShape(RoundedRectangle(cornerRadius: ButtonMetrics.radius))
            }
        }
        .accessibilityLabel(label)
    }
}
