import SwiftUI

/// Rohbau – Übersicht (Nr. 17, IosRohbau.dc.html): the shared protocol template with the warning banner, its
/// chevron and the defect row mark. It has no PDF button. Core tokens only.
struct RohbauScreen: View {
    let model: TradeProtocolModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (ProtocolAction) -> Void

    var body: some View {
        ProtocolTemplate(model: model, backLabel: backLabel, onBack: onBack, onAction: onAction)
    }
}
