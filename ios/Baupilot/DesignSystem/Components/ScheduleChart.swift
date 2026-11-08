import SwiftUI

/// Colour key above the chart: squares for the bar states, a diamond for "Anfragen" (IosZeitplan:38-43).
public struct ScheduleLegend: View {
    public enum Item {
        case square(String, ColorToken)
        case diamond(String)
    }

    private enum Metrics {
        static let gap: CGFloat = 14
        static let innerGap: CGFloat = 5
        static let swatch: CGFloat = 10
        static let swatchRadius: CGFloat = 3
    }

    private let items: [Item]

    public init(items: [Item]) {
        self.items = items
    }

    public var body: some View {
        HStack(spacing: Metrics.gap) {
            ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                HStack(spacing: Metrics.innerGap) {
                    switch item {
                    case let .square(label, color):
                        RoundedRectangle(cornerRadius: Metrics.swatchRadius)
                            .fill(color.color)
                            .frame(width: Metrics.swatch, height: Metrics.swatch)
                        Text(label)
                    case let .diamond(label):
                        DiamondMarker(size: .s8)
                        Text(label)
                    }
                }
                .interText(12, .regular)
                .foregroundStyle(Tokens.muted.color)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

/// Time chart of all trades. It draws the boxes and labels of a `ScheduleLayout` where they are and does
/// no geometry math of its own: boxes use the snapped `draw*` values, text the unsnapped x (I9, D22).
public struct ScheduleChart: View {
    private enum Metrics {
        static let header: CGFloat = 20
        static let content: CGFloat = 33
        static let border: CGFloat = 1
        static let barTop: CGFloat = 11
        static let barHeight: CGFloat = 12
        static let barRadius: CGFloat = 4
        static let diamondTop: CGFloat = 13
        static let diamondSide: CGFloat = 8
        static let todayWidth: CGFloat = 2
        static let labelPadding: CGFloat = 4
    }

    private let layout: ScheduleLayout
    private let todayLabel: String
    private let accessibilityLabel: String

    public init(layout: ScheduleLayout, todayLabel: String, accessibilityLabel: String) {
        self.layout = layout
        self.todayLabel = todayLabel
        self.accessibilityLabel = accessibilityLabel
    }

    private func barColor(_ state: ScheduleState) -> ColorToken {
        switch state {
        case .done: return Tokens.done
        case .running: return Tokens.accent
        case .planned: return Tokens.plan
        }
    }

    private var monthHeader: some View {
        ZStack(alignment: .topLeading) {
            ForEach(Array(layout.monthLabels.enumerated()), id: \.offset) { _, month in
                Text(month.label)
                    .interText(11, .semibold)
                    .foregroundStyle(Tokens.muted.color)
                    .fixedSize()
                    .padding(.leading, month.x)
            }
        }
        .frame(maxWidth: .infinity, minHeight: Metrics.header, maxHeight: Metrics.header, alignment: .topLeading)
    }

    private func rowView(_ row: ScheduleRowLayout) -> some View {
        ZStack(alignment: .topLeading) {
            Rectangle().fill(Tokens.border.core.color).frame(height: Metrics.border)
            VStack(spacing: 0) {
                Text(row.name)
                    .interText(13, row.state == .running ? .bold : .medium)
                    .truncationMode(.tail)
                    .foregroundStyle(Tokens.text.color)
            }
            .frame(width: ScheduleLayout.nameColumn, height: Metrics.content, alignment: .leading)
            .padding(.top, Metrics.border)
            RoundedRectangle(cornerRadius: Metrics.barRadius)
                .fill(barColor(row.state).color)
                .frame(width: row.drawWidth, height: Metrics.barHeight)
                .padding(.leading, row.drawX)
                .padding(.top, Metrics.border + Metrics.barTop)
            if let center = row.diamondDrawCenterX {
                DiamondMarker(size: .s8, outline: Tokens.bg)
                    .padding(.leading, center - Metrics.diamondSide / 2)
                    .padding(.top, Metrics.border + Metrics.diamondTop)
            }
        }
        .frame(maxWidth: .infinity, minHeight: ScheduleLayout.rowPitch, maxHeight: ScheduleLayout.rowPitch,
               alignment: .topLeading)
    }

    private var rows: some View {
        ZStack(alignment: .topLeading) {
            VStack(spacing: 0) {
                ForEach(layout.rows, id: \.tradeId) { rowView($0) }
            }
            Rectangle()
                .fill(Tokens.text.color)
                .frame(width: Metrics.todayWidth)
                .padding(.leading, layout.todayDrawX)
        }
        .frame(height: CGFloat(layout.rows.count) * ScheduleLayout.rowPitch, alignment: .topLeading)
    }

    private var todayRow: some View {
        VStack(alignment: .leading, spacing: Metrics.labelPadding) {
            Color.clear.frame(height: 0)
            Text(todayLabel)
                .interText(11, .bold)
                .foregroundStyle(Tokens.text.color)
                .fixedSize()
                .padding(.leading, layout.todayLabelX)
        }
        .frame(maxWidth: .infinity, alignment: .topLeading)
    }

    public var body: some View {
        VStack(spacing: 0) {
            monthHeader
            rows
            todayRow
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(accessibilityLabel)
        .accessibilityAddTraits(.isImage)
    }
}
