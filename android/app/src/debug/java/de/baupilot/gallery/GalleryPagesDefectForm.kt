package de.baupilot.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.components.AlertBanner
import de.baupilot.designsystem.components.BaupilotOutlinedTextArea
import de.baupilot.designsystem.components.BaupilotOutlinedTextField
import de.baupilot.designsystem.components.FieldLook
import de.baupilot.designsystem.components.SegmentedButtons

@Composable
internal fun DefectFormPage() {
    var description by remember { mutableStateOf("Flügel fällt ab ca. 45° Öffnung selbstständig wieder zu.") }
    var date by remember { mutableStateOf("15.01.2027") }
    Caption("AlertBanner · errBg / errFg, Radius 12, Innenabstand 12 16, Lücke 12, Symbol 20 (AndAbnMangel:27-33)")
    AlertBanner(
        icon = BaupilotIcon.AlertTriangleW22,
        title = "F2 · Wohnen · Schritt 1",
        subtitle = "Drehstellung: Flügel bleibt nicht stehen",
    )
    Caption("BaupilotOutlinedTextArea · 3 Zeilen, 98 hoch, Rand 2 primary (AndAbnMangel:35-38)")
    BaupilotOutlinedTextArea(label = "Beschreibung", value = description, onValueChange = { description = it })
    Caption("SegmentedButtons · 42 außen, Radius 20, drei Zellen, mittlere gewählt (AndAbnMangel:55-61)")
    SegmentedButtons(
        items = listOf("Gering", "Mittel", "Erheblich"),
        selectedIndex = 1,
        selectedPrefix = "✓ ",
        identifiers = listOf("gallery.severity.0", "gallery.severity.1", "gallery.severity.2"),
        onSelect = {},
    )
    Caption("BaupilotOutlinedTextField · Kalender-Symbol am Ende (AndAbnMangel:64-69)")
    BaupilotOutlinedTextField(
        label = "Behebung bis",
        value = date,
        onValueChange = { date = it },
        placeholder = "",
        look = FieldLook.Idle,
        trailingIcon = BaupilotIcon.Calendar,
    )
}
