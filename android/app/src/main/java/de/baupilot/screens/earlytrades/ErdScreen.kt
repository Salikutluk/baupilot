package de.baupilot.screens.earlytrades

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.baupilot.app.TradeProtocolModel

/** Erdarbeiten – Protokoll (Nr. 16, AndErd.dc.html): the shared protocol template with the accepted banner and its PDF button. */
@Composable
fun ErdScreen(
    model: TradeProtocolModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (ProtocolAction) -> Unit = {},
) = ProtocolTemplate(model, onBack, modifier, onAction)
