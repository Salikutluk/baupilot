package de.baupilot.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.AssistChip
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.ExtendedFab
import de.baupilot.designsystem.components.FabIcon
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.FilterChip
import de.baupilot.designsystem.components.ObjectChip
import de.baupilot.designsystem.components.ObjectChipState
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.OutlinedIconButton
import de.baupilot.designsystem.components.StatusChip
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize
import de.baupilot.designsystem.components.TextButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Wrap(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

private val statusColors = listOf(
    "Fertig" to StatusChipColors.Ok(),
    "Vergleichen" to StatusChipColors.Info,
    "Mangel" to StatusChipColors.Error,
    "Mit Vorbehalt" to StatusChipColors.Warn(),
    "Läuft" to StatusChipColors.Running,
    "Offen" to StatusChipColors.Open,
    "Ausstehend" to StatusChipColors.Pending,
    "Geplant" to StatusChipColors.Neutral,
    "Heute" to StatusChipColors.Outline,
)

@Composable
internal fun ChipsPage() {
    Caption("AssistChip · mit Haken (AndLogin:27)")
    Wrap {
        AssistChip("Zeitplan", leadingCheck = true)
        AssistChip("Angebote", leadingCheck = true)
        AssistChip("Dokumente", leadingCheck = true)
    }
    Caption("FilterChip · gewählt, ungewählt, gewählt mit Haken")
    Wrap {
        FilterChip("Offen", selected = true)
        FilterChip("Alle", selected = false)
        FilterChip("Erledigt", selected = true, showCheck = true)
    }
    Caption("ObjectChip · done, now, open")
    Wrap {
        ObjectChip("Bad", ObjectChipState.Done)
        ObjectChip("Küche", ObjectChipState.Now)
        ObjectChip("Flur", ObjectChipState.Open)
    }
    Caption("StatusChip · H32")
    Wrap { statusColors.forEach { (text, colors) -> StatusChip(text, colors, StatusChipSize.H32) } }
    Caption("StatusChip · H28")
    Wrap { statusColors.forEach { (text, colors) -> StatusChip(text, colors, StatusChipSize.H28) } }
    Caption("StatusChip · Auto und H24")
    Wrap {
        StatusChip("In Ordnung", StatusChipColors.Ok(), StatusChipSize.Auto)
        StatusChip("1 Mangel", StatusChipColors.Error, StatusChipSize.Auto)
        StatusChip("Vorhanden", StatusChipColors.Info, StatusChipSize.H24)
        StatusChip("Offen", StatusChipColors.Neutral, StatusChipSize.H24)
    }
}

@Composable
internal fun ButtonsPage() {
    Caption("FilledButton · regular")
    FilledButton("Anmelden", ButtonSize.Regular, FilledContainer.Primary)
    Caption("FilledButton · compact, error, regular error, mit Icon")
    Wrap {
        FilledButton("Behoben prüfen", ButtonSize.Compact, FilledContainer.Primary)
        FilledButton("Erinnern", ButtonSize.Compact, FilledContainer.Error)
    }
    FilledButton("Mangel", ButtonSize.Regular, FilledContainer.Error)
    FilledButton("Foto aufnehmen", ButtonSize.Regular, FilledContainer.Primary, icon = BaupilotIcon.Camera)
    Caption("OutlinedButton · text, primary, err, onPrimaryContainer")
    OutlinedButton("Abbrechen", contentColor = Tokens.text)
    OutlinedButton("Rückfrage", contentColor = Tokens.primary)
    OutlinedButton("Mangel", contentColor = Tokens.err)
    OutlinedButton("Erinnern", contentColor = Tokens.onPrimaryContainer)
    Caption("OutlinedButton · borderOutside")
    OutlinedButton("Mit Google anmelden", borderOutside = true)
    Caption("OutlinedIconButton · TextButton")
    Wrap {
        OutlinedIconButton()
        TextButton("Neues Bauvorhaben anlegen")
    }
    Caption("ExtendedFab · plus und play")
    Wrap {
        ExtendedFab("Dokument", FabIcon.Plus)
        ExtendedFab("Weiter: F8 · Schlafen", FabIcon.Play)
    }
}
