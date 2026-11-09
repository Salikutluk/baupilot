import SwiftUI

enum ElementsAction {
    case open(AppRoute)
}

/// Fenster – Elemente (IosAbnElemente.dc.html).
struct ElementsScreen: View {
    let model: ElementsModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (ElementsAction) -> Void

    var body: some View {
        ScreenScaffold(kind: .detail) {
            BackBar(label: backLabel, action: onBack)
            DetailTitle(model.title, subtitle: model.subtitle, style: .size30)
            summaryCard
            ForEach(model.floors, id: \.title) { floor in
                floorSection(floor)
            }
        } bottomBar: {
            BottomActionBar {
                PrimaryButton(model.nextLabel, action: { onAction(.open(model.nextTarget)) })
                    .accessibilityIdentifier("inspection.next")
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.inspection.\(model.tradeId)")
    }

    /// IosAbnElemente:24-37: counter and guide link on one baseline, the segmented bar, the legend; gap 10.
    private var summaryCard: some View {
        Card {
            VStack(alignment: .leading, spacing: 10) {
                HStack(alignment: .firstTextBaseline) {
                    Text(model.counter)
                        .interText(16, .semibold)
                        .foregroundStyle(Tokens.text.color)
                    Spacer(minLength: 0)
                    InlineLink(model.guideLinkLabel, size: 14, action: nil)
                }
                SegmentedProgress(
                    segments: model.progress.map(segmentColor), height: 8, accessibilityLabel: model.counter)
                ProgressLegend(
                    items: model.legend.map { ProgressLegend.Item(label: $0.label, color: segmentColor($0.status)) })
            }
        }
    }

    private func floorSection(_ floor: ElementsModel.FloorSection) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                floor.title, style: .caps(inset: 4, link: SectionHeader.Link(title: floor.moreLabel, size: 13, action: nil)))
            Card(padding: EdgeInsets()) {
                VStack(alignment: .leading, spacing: 0) {
                    ForEach(Array(floor.items.enumerated()), id: \.element.id) { index, item in
                        elementRow(item, index: index)
                    }
                }
            }
        }
    }

    private func elementRow(_ item: ElementsModel.ElementRow, index: Int) -> some View {
        let target = item.target
        return ListRow(
            title: item.room, subtitle: item.type,
            leading: .textTile(item.id, .s36),
            trailing: .badge(StatusBadge(item.label, tone: tone(item.status))),
            density: .minHeight(54), showsDivider: index > 0,
            action: target.map { route in { onAction(.open(route)) } }
        )
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("inspection.element.\(item.id)")
    }

    /// IosAbnElemente:25-27, `segs`: ok `ok`, defect `err`, open `track`.
    private func segmentColor(_ status: ElementStatus) -> ColorToken {
        switch status {
        case .ok: return Tokens.ok.core
        case .defect: return Tokens.err.abnElemente
        case .now, .open: return Tokens.track.abnElemente
        }
    }

    /// IosAbnElemente:61, `S`: ok `oSoft/oFg`, defect `eSoft/eFg`, now `aSoft/aFg`, open `chip/muted`.
    private func tone(_ status: ElementStatus) -> StatusBadge.Tone {
        switch status {
        case .ok: return .ok
        case .defect: return .error()
        case .now: return .accent
        case .open: return .neutral(bg: Tokens.chip.core)
        }
    }
}
