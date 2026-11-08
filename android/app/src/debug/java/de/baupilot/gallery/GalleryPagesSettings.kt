package de.baupilot.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.AvatarSize
import de.baupilot.designsystem.components.BaupilotAvatar
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotSwitch
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.TonalButton
import de.baupilot.designsystem.components.Trailing

/** Only the parts the Profil screen added: both avatar sizes, the profile row and the settings rows. */
@Composable
internal fun SettingsPage() {
    Caption("BaupilotAvatar · S40 (16 / 500) und S56 (20 / 500)")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        BaupilotAvatar("MM", AvatarSize.S40, Tokens.primaryContainer, Tokens.onPrimaryContainer)
        BaupilotAvatar("MM", AvatarSize.S56, Tokens.primaryContainer, Tokens.onPrimaryContainer)
    }
    Caption("Profilzeile · Avatar S56, Texte, TonalButton")
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BaupilotAvatar("MM", AvatarSize.S56, Tokens.primaryContainer, Tokens.onPrimaryContainer)
        BaupilotListItem(
            title = "Max Mustermann",
            subtitle = "max@beispiel.de",
            minHeight = ListItemHeight.H48,
            horizontalPadding = 0.dp,
            modifier = Modifier.weight(1f),
        )
        TonalButton("Bearbeiten")
    }
    Caption("Einstellungszeile · H56, Icon, Schalter an und aus")
    BaupilotListItem(
        title = "Fristen & Termine",
        subtitle = "Erinnerung 3 Tage vorher",
        leading = Leading.Icon(BaupilotIcon.BellSimple),
        trailing = Trailing.Switch { BaupilotSwitch(true, "Fristen & Termine", {}) },
        minHeight = ListItemHeight.H56,
    )
    BaupilotListItem(
        title = "Wochenrückblick",
        leading = Leading.Icon(BaupilotIcon.Calendar),
        trailing = Trailing.Switch { BaupilotSwitch(false, "Wochenrückblick", {}) },
        minHeight = ListItemHeight.H56,
    )
    Caption("Einstellungszeile · Abmelden in error")
    BaupilotListItem(
        title = "Abmelden",
        leading = Leading.Icon(BaupilotIcon.Logout, Tokens.error),
        minHeight = ListItemHeight.H56,
        titleColor = Tokens.error,
    )
}
