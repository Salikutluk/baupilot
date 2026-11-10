package de.baupilot.screens.earlytrades

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.baupilot.app.TradeProtocolModel

/** Zimmerer – Dachstuhl (Nr. 19, AndErd.dc.html template): the shared protocol template with the accepted banner and its PDF button. */
@Composable
fun ZimmererScreen(
    model: TradeProtocolModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (ProtocolAction) -> Unit = {},
) = ProtocolTemplate(model, onBack, modifier, onAction)
