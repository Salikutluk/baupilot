import SwiftUI

enum GewerkAction {
    case openDocument(String)
    case openCompare(String)
}

/// Gewerk-Detail (IosGewerk.dc.html): back bar, title block, fact strip, offers, next steps, action bar.
struct GewerkScreen: View {
    let model: GewerkModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (GewerkAction) -> Void

    var body: some View {
        ScreenScaffold(kind: .detail, sectionSpacing: .medium) {
            BackBar(
                label: backLabel, action: onBack,
                trailing: .icon(.moreHorizontal, size: 22, label: model.moreLabel, action: nil))
            titleBlock
            FactStrip(items: model.facts.map { (label: $0.label, value: $0.value) }, style: .divided)
            offers
            steps
        } bottomBar: {
            BottomActionBar {
                HStack(spacing: 10) {
                    PrimaryButton(model.compareLabel, action: compareAction)
                        .accessibilityIdentifier("trade.compare")
                    SecondaryIconButton(icon: .plusW22, label: model.addLabel, action: nil)
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.trade.\(model.tradeId)")
    }

    private var compareAction: (() -> Void)? {
        guard case let .compare(tradeId)? = model.compareTarget else { return nil }
        return { onAction(.openCompare(tradeId)) }
    }

    private var titleBlock: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(model.title)
                .interText(32, .bold, tracking: -0.6)
                .foregroundStyle(Tokens.text.color)
                .accessibilityAddTraits(.isHeader)
            HStack(spacing: 8) {
                Chip(model.statusLabel, tone: .accent)
                Chip(model.phaseLabel, tone: .neutral())
            }
        }
    }

    private var offers: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionHeader(model.offersHeading, style: .title(note: model.offersSummary))
            Card(padding: EdgeInsets()) {
                VStack(spacing: 0) {
                    ForEach(Array(model.offers.enumerated()), id: \.element.id) { index, offer in
                        offerRow(offer, index: index)
                    }
                }
            }
        }
    }

    private func offerRow(_ offer: GewerkModel.OfferRow, index: Int) -> some View {
        ListRow(
            title: offer.firm, subtitle: offer.meta,
            leading: .iconTile(.document, size: .s38),
            trailing: .amount(
                offer.price, size: 15, weight: .bold,
                color: offer.pending ? Tokens.muted : Tokens.text,
                badge: offer.best
                    ? StatusBadge(model.bestLabel, tone: .ok, size: .mini, weight: .bold) : nil),
            density: .padded(v: 12), showsDivider: index > 0,
            action: documentAction(offer.target)
        )
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("trade.offer.\(offer.id)")
    }

    private func documentAction(_ target: AppRoute?) -> (() -> Void)? {
        guard case let .document(id)? = target else { return nil }
        return { onAction(.openDocument(id)) }
    }

    private var steps: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(model.stepsHeading)
                .interText(20, .bold)
                .foregroundStyle(Tokens.text.color)
                .accessibilityAddTraits(.isHeader)
            VStack(alignment: .leading, spacing: 10) {
                ForEach(Array(model.steps.enumerated()), id: \.offset) { _, step in
                    ChecklistRow(title: step.label, done: step.done)
                }
            }
        }
    }
}
