package de.baupilot.gallery

import androidx.compose.runtime.Composable
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.NotificationListItem

@Composable
internal fun NotificationsPage() {
    Caption("NotificationListItem · ungelesen (AndMitteilungen:30)")
    NotificationListItem(
        icon = BaupilotIcon.AlertTriangle,
        iconBackground = Tokens.errBg,
        iconForeground = Tokens.errFg,
        title = "Frist überschritten",
        whenLabel = "08:10",
        text = "Rohbau · Riss in Kellerwand Nord, seit 3 Tagen offen",
        unread = true,
        onClick = null,
    )
    Caption("NotificationListItem · gelesen")
    NotificationListItem(
        icon = BaupilotIcon.Document,
        iconBackground = Tokens.infoBg,
        iconForeground = Tokens.infoFg,
        title = "Neues Angebot",
        whenLabel = "Mo",
        text = "Fließestrich Hahn · 10.450 € – jetzt vergleichen",
        unread = false,
        onClick = null,
    )
}
