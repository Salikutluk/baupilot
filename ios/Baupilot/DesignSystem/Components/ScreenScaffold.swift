import SwiftUI

/// Page background, prototype paddings and scrolling content. Ignores the safe areas and applies
/// the prototype paddings literally; the bottom bar is overlaid at the bottom edge.
public struct ScreenScaffold<Content: View, BottomBar: View>: View {
    public enum Kind {
        case tabRoot, detail, detailNoBar, modal, `protocol`

        var top: CGFloat {
            switch self {
            case .tabRoot: return Tokens.Layout.topInsetLargeTitle
            default: return Tokens.Layout.topInsetDetail
            }
        }

        var bottom: CGFloat {
            switch self {
            case .tabRoot: return 100
            case .detail: return 110
            case .detailNoBar: return 40
            case .modal: return 32
            case .protocol: return 24
            }
        }
    }

    public enum SectionSpacing {
        case regular, loose, medium, tight

        var value: CGFloat {
            switch self {
            case .regular: return 14
            case .loose: return 18
            case .medium: return 16
            case .tight: return 12
            }
        }
    }

    private let kind: Kind
    private let sectionSpacing: SectionSpacing
    private let content: Content
    private let bottomBar: BottomBar

    public init(
        kind: Kind = .tabRoot,
        sectionSpacing: SectionSpacing = .regular,
        @ViewBuilder content: () -> Content,
        @ViewBuilder bottomBar: () -> BottomBar
    ) {
        self.kind = kind
        self.sectionSpacing = sectionSpacing
        self.content = content()
        self.bottomBar = bottomBar()
    }

    public var body: some View {
        ZStack(alignment: .bottom) {
            Tokens.bg.color
            ScrollView(.vertical) {
                VStack(alignment: .leading, spacing: sectionSpacing.value) {
                    content
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.top, kind.top)
                .padding(.bottom, kind.bottom)
                .padding(.horizontal, Tokens.Layout.gutter)
            }
            .scrollIndicators(.hidden)
            bottomBar
        }
        .foregroundStyle(Tokens.text.color)
        .ignoresSafeArea()
    }
}

public extension ScreenScaffold where BottomBar == EmptyView {
    init(
        kind: Kind = .tabRoot,
        sectionSpacing: SectionSpacing = .regular,
        @ViewBuilder content: () -> Content
    ) {
        self.init(kind: kind, sectionSpacing: sectionSpacing, content: content) { EmptyView() }
    }
}
