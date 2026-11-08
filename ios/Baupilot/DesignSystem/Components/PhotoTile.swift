import SwiftUI

/// Photo, guide or add tile of the inspection flows (IosAbnPruefung:50-52, IosAbnMangel:40-41).
/// The tile is as wide as its container and a fixed 84 pt or 76 pt tall.
public struct PhotoTile: View {
    public enum Kind {
        case photo(Image, caption: String?, checked: Bool)
        case guide(title: String, illustration: AnyView)
        case add(label: String)
    }

    public enum Height: CGFloat {
        case h84 = 84
        case h76 = 76
    }

    private let kind: Kind
    private let height: Height
    private let accessibilityLabel: String?
    private let chip: ColorToken

    /// `chip` is the guide tile's background; it varies per screen (IosAbnMangel:41).
    /// `accessibilityLabel` overrides the spoken label, for a guide tile without visible title
    /// whose illustration carries the `role=img` label.
    public init(
        kind: Kind, height: Height, chip: ColorToken = Tokens.chip.core,
        accessibilityLabel: String? = nil
    ) {
        self.chip = chip
        self.kind = kind
        self.height = height
        self.accessibilityLabel = accessibilityLabel
    }

    private var shape: RoundedRectangle {
        RoundedRectangle(cornerRadius: 10)
    }

    public var body: some View {
        content
            .frame(maxWidth: .infinity)
            .frame(height: height.rawValue)
    }

    @ViewBuilder
    private var content: some View {
        switch kind {
        case .photo(let image, let caption, let checked):
            photo(image, caption: caption, checked: checked)
        case .guide(let title, let illustration):
            guide(title: title, illustration: illustration)
        case .add(let label):
            add(label: label)
        }
    }

    private func photo(_ image: Image, caption: String?, checked: Bool) -> some View {
        Color.clear
            .overlay {
                image
                    .resizable()
                    .scaledToFill()
            }
            .overlay(alignment: .topTrailing) {
                if checked {
                    Icon(.checkW35, size: 11, color: Tokens.onPhoto.color)
                        .frame(width: 20, height: 20)
                        .background(Tokens.photoBadge.color, in: Circle())
                        .padding(.top, 6)
                        .padding(.trailing, 6)
                }
            }
            .overlay(alignment: .bottom) {
                if let caption {
                    Text(caption)
                        .interText(11, .semibold)
                        .foregroundStyle(Tokens.onPhoto.color)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 4)
                        .background(Tokens.photoCaption.color)
                }
            }
            .clipShape(shape)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(accessibilityLabel ?? caption ?? "")
            .accessibilityAddTraits(.isImage)
    }

    private func guide(title: String, illustration: AnyView) -> some View {
        VStack(spacing: 4) {
            illustration
            if !title.isEmpty {
                Text(title)
                    .interText(11, .semibold)
                    .foregroundStyle(Tokens.muted.color)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(chip.color, in: shape)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityLabel ?? title)
        .accessibilityAddTraits(.isImage)
    }

    private func add(label: String) -> some View {
        VStack(spacing: 4) {
            Icon(.camera, size: 22, color: Tokens.accent.color)
            Text(label)
                .interText(12, .semibold)
                .foregroundStyle(Tokens.accent.color)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .overlay {
            shape.strokeBorder(
                Tokens.accent.color,
                style: StrokeStyle(lineWidth: 1, dash: [3, 2]))
        }
        .contentShape(shape)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityLabel ?? label)
        .accessibilityAddTraits(.isButton)
    }
}
