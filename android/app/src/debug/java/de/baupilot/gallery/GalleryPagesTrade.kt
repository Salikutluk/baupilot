package de.baupilot.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BaupilotTabRow
import de.baupilot.designsystem.components.BottomActionBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.FactRow
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.OfferItem
import de.baupilot.designsystem.components.TagChip
import de.baupilot.designsystem.components.TagChipStyle
import de.baupilot.designsystem.components.TonalIconButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TradePage() {
    Caption("TagChip · gefüllt und umrandet (AndGewerk:25)")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TagChip("Angebote einholen", TagChipStyle.Filled)
        TagChip("Phase 8 · Estrich", TagChipStyle.Outlined)
    }
    Caption("FactRow · drei Zeilen auf surfaceContainer (AndGewerk:26)")
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Tokens.Radius.card))
            .background(Tokens.surfaceContainer.color()),
    ) {
        FactRow(BaupilotIcon.CalendarW22, "Zeitfenster", "KW 8–9")
        FactRow(BaupilotIcon.ClockW22, "Angebotsfrist", "15.01.2027")
        FactRow(BaupilotIcon.Link, "Wartet auf", "Installateur")
    }
    Caption("BaupilotTabRow · erster Tab gewählt (AndGewerk:46)")
    BaupilotTabRow(
        tabs = listOf("Angebote (3)", "Termine", "Notizen"),
        selectedIndex = 0,
        contentDescription = "Bereiche",
    )
    Caption("OfferItem · günstigstes, normal, ausstehend (AndGewerk:55)")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OfferItem("Estrichbau Wagner", "PDF · 10.12.2026", "9.800 €", true, "Günstigstes", false, Tokens.okBg.gewerk)
        OfferItem("Fließestrich Hahn", "PDF · 14.12.2026", "10.450 €", false, "Günstigstes", false, Tokens.okBg.gewerk)
        OfferItem("Bodenbau Lorenz", "Angefragt am 05.12.", "Ausstehend", false, "Günstigstes", true, Tokens.okBg.gewerk)
    }
    Caption("BottomActionBar · TonalIconButton und Hauptaktion (AndGewerk:66)")
    BottomActionBar { ActionBarSample() }
}

@Composable
private fun RowScope.ActionBarSample() {
    TonalIconButton(BaupilotIcon.Paperclip, "Dokument hinzufügen")
    FilledButton(
        label = "Angebote vergleichen",
        size = ButtonSize.Regular,
        container = FilledContainer.Primary,
        modifier = Modifier.weight(1f),
    )
}
