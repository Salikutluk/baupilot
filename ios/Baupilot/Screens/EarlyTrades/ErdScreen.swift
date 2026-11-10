import SwiftUI

/// Erdarbeiten – Protokoll (Nr. 16, IosErd.dc.html): the shared protocol template with the accepted banner and
/// its PDF button. Core tokens only.
struct ErdScreen: View {
    let model: TradeProtocolModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (ProtocolAction) -> Void

    var body: some View {
        ProtocolTemplate(model: model, backLabel: backLabel, onBack: onBack, onAction: onAction)
    }
}
