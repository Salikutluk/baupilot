import SwiftUI

/// Zimmerer – Dachstuhl (Nr. 19, IosErd.dc.html template): the shared protocol template with the accepted banner
/// and its PDF button. Core tokens only.
struct ZimmererScreen: View {
    let model: TradeProtocolModel
    let backLabel: String
    let onBack: () -> Void
    let onAction: (ProtocolAction) -> Void

    var body: some View {
        ProtocolTemplate(model: model, backLabel: backLabel, onBack: onBack, onAction: onAction)
    }
}
