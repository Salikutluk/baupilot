import SwiftUI

/// Photo, scene, guide, icon or add tile of the inspection flows (IosAbnPruefung:50-52, IosAbnMangel:40-41).
/// The tile is as wide as its container and a fixed 84 pt or 76 pt tall.
public struct PhotoTile: View {
    public enum Kind {
        case photo(Image, caption: String?, checked: Bool)
        /// A drawn scene filling the tile (IosAbnPruefung:51), with the same badge and caption as `.photo`.
        case scene(AnyView, caption: String?, checked: Bool)
        case guide(title: String, illustration: AnyView)
        /// Centred 22 pt `muted` icon on the tile fill; `fill` defaults to the tile's `chip`.
        case icon(BaupilotIcon, fill: ColorToken? = nil)
        /// Dashed camera tile; `nil` draws the camera only.
        case add(label: String?)
    }

    /// The tick badge of a checked photo or scene tile (IosAbnPruefung:51): 20 pt, 6 pt from the top-right corner.
    struct Badge: View {
        static let size: CGFloat = 20
        static let inset: CGFloat = 6

        var body: some View {
            Icon(.checkW35, size: 11, color: Tokens.onPhoto.color)
                .frame(width: Self.size, height: Self.size)
                .background(Tokens.photoBadge.color, in: Circle())
        }
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
        case .scene(let scene, let caption, let checked):
            photo(scene, caption: caption, checked: checked)
        case .guide(let title, let illustration):
            guide(title: title, illustration: illustration)
        case .icon(let icon, let fill):
            iconTile(icon, fill: fill ?? chip)
        case .add(let label):
            add(label: label)
        }
    }

    private func photo(_ image: Image, caption: String?, checked: Bool) -> some View {
        photo(
            AnyView(image.resizable().scaledToFill()), caption: caption, checked: checked)
    }

    private func photo(_ content: AnyView, caption: String?, checked: Bool) -> some View {
        Color.clear
            .overlay { content }
            .overlay(alignment: .topTrailing) {
                if checked {
                    Badge()
                        .padding(.top, Badge.inset)
                        .padding(.trailing, Badge.inset)
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

    private func iconTile(_ icon: BaupilotIcon, fill: ColorToken) -> some View {
        Icon(icon, size: 22, color: Tokens.muted.color)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(fill.color, in: shape)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(accessibilityLabel ?? "")
            .accessibilityAddTraits(.isImage)
    }

    private func add(label: String?) -> some View {
        VStack(spacing: 4) {
            Icon(.camera, size: 22, color: Tokens.accent.color)
            if let label {
                Text(label)
                    .interText(12, .semibold)
                    .foregroundStyle(Tokens.accent.color)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .overlay {
            shape.strokeBorder(
                Tokens.accent.color,
                style: StrokeStyle(lineWidth: 1, dash: [3, 2]))
        }
        .contentShape(shape)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityLabel ?? label ?? "")
        .accessibilityAddTraits(.isButton)
    }
}
