#if DEBUG
import SwiftUI

/// Every generated icon at 24 pt in `text`, with its asset name, six per row.
struct GalleryIconsPage: View {
    private static let columns = 6

    private var rows: [[BaupilotIcon]] {
        let all = BaupilotIcon.allCases
        return stride(from: 0, to: all.count, by: Self.columns).map {
            Array(all[$0..<min($0 + Self.columns, all.count)])
        }
    }

    var body: some View {
        GalleryGroup("BaupilotIcon.allCases · 24 pt") {
            VStack(spacing: 8) {
                ForEach(rows.indices, id: \.self) { index in
                    HStack(alignment: .top, spacing: 4) {
                        ForEach(rows[index], id: \.self) { icon in
                            cell(icon)
                        }
                        ForEach(0..<(Self.columns - rows[index].count), id: \.self) { _ in
                            Color.clear.frame(maxWidth: .infinity)
                        }
                    }
                }
            }
        }
    }

    private func cell(_ icon: BaupilotIcon) -> some View {
        VStack(spacing: 4) {
            Icon(icon, size: 24, color: Tokens.text.color)
            Text(icon.rawValue.replacingOccurrences(of: "ic_", with: "", options: .anchored))
                .font(.inter(9, .medium))
                .foregroundStyle(Tokens.muted.color)
                .multilineTextAlignment(.center)
                .lineLimit(2)
        }
        .frame(maxWidth: .infinity, alignment: .top)
    }
}
#endif
