package de.baupilot.screens.earlytrades

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.baupilot.app.TradeProtocolModel

/** Rohbau – Übersicht (Nr. 17, AndRohbau.dc.html): the shared protocol template with the warning banner, its chevron and the defect row mark. It has no PDF button. */
@Composable
fun RohbauScreen(
    model: TradeProtocolModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (ProtocolAction) -> Unit = {},
) = ProtocolTemplate(model, onBack, modifier, onAction)
