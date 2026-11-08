package de.baupilot.gallery

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotSwitch
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize
import de.baupilot.designsystem.components.Trailing

@Composable
internal fun ListsPage() {
    Caption("ListItem · Avatar mit Status-Chip (64)")
    BaupilotListItem(
        title = "Elektriker",
        subtitle = "ab KW 3 · Rohinstallation",
        leading = Leading.Avatar.initials("El", Tokens.secondaryContainer, Tokens.onSecondaryContainer),
        trailing = Trailing.StatusChip("Beauftragt", StatusChipColors.Ok(), StatusChipSize.H28),
        minHeight = ListItemHeight.H64,
    )
    Caption("ListItem · Dokument mit Betrag (72)")
    BaupilotListItem(
        title = "2. Abschlagsrechnung",
        subtitle = "Fenster & Türen · offen",
        leading = Leading.Icon(BaupilotIcon.DocumentW22),
        trailing = Trailing.Text("18.400 €", size = 14.sp, color = Tokens.text),
        minHeight = ListItemHeight.H72,
        ellipsis = true,
    )
    Caption("ListItem · Status-Avatar mit hi-Hintergrund (60)")
    BaupilotListItem(
        title = "Rechnung bald fällig",
        subtitle = "2. Abschlag 18.400 € bis 22.12.",
        leading = Leading.Avatar.icon(BaupilotIcon.Clock, Tokens.hi, Tokens.primary),
        minHeight = ListItemHeight.H60,
    )
    Caption("ListItem · Farbmuster (56)")
    BaupilotListItem(
        title = "Küche",
        subtitle = "Weiß, Latex abwaschbar",
        leading = Leading.Swatch(Tokens.lit),
        minHeight = ListItemHeight.H56,
    )
    Caption("ListItem · Einstellung mit Switch (56)")
    BaupilotListItem(
        title = "Fristen & Termine",
        subtitle = "Erinnerung 3 Tage vorher",
        trailing = Trailing.Switch {
            BaupilotSwitch(checked = true, contentDescription = "Fristen & Termine", onCheckedChange = {})
        },
        minHeight = ListItemHeight.H56,
    )
    Caption("ListItem · Mitteilung ungelesen")
    BaupilotListItem(
        title = "Frist überschritten",
        subtitle = "Rohbau · Riss in Kellerwand Nord, seit 3 Tagen offen",
        leading = Leading.Avatar.icon(BaupilotIcon.AlertTriangle, Tokens.errBg, Tokens.errFg),
        background = Tokens.unread,
        titleWeight = 700,
    )
}

@Composable
internal fun CardsPage() {
    Caption("Card · surfaceContainer")
    BaupilotCard { CardText("Rohbau") }
    Caption("Card · primaryContainer hero")
    BaupilotCard(container = Tokens.primaryContainer) {
        BaupilotText(text = "Elektriker", size = 16.sp, weight = 500, color = Tokens.onPrimaryContainer)
    }
    Caption("Card · outlined")
    BaupilotCard(container = Tokens.bg, border = Tokens.outlineVariant) { CardText("Dachdecker") }
    Caption("Card · r12 mit divider-Rand")
    BaupilotCard(
        container = Tokens.bg,
        radius = Tokens.Radius.listCard,
        border = Tokens.divider,
        padding = PaddingValues(12.dp, 16.dp),
    ) { CardText("Estrich") }
}

@Composable
private fun CardText(text: String) {
    BaupilotText(text = text, size = 16.sp, color = Tokens.text)
}
