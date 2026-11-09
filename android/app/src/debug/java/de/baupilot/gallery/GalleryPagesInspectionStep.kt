package de.baupilot.gallery

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.components.PhotoTile
import de.baupilot.designsystem.components.PhotoTileKind
import de.baupilot.designsystem.components.WindowLevelSketch

@Composable
internal fun InspectionStepPage() {
    Caption("PhotoTile · Guide, Scene mit Haken, Add · Höhe 80, Radius 12, Raster 3 × 100,667 (AndAbnPruefung:57-59)")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PhotoTile(
            kind = PhotoTileKind.Guide("So prüfen") { WindowLevelSketch() },
            height = 80.dp,
            modifier = Modifier.width(100.667.dp),
        )
        PhotoTile(
            kind = PhotoTileKind.Scene("Foto oben", checked = true),
            height = 80.dp,
            modifier = Modifier.width(100.667.dp),
        )
        PhotoTile(
            kind = PhotoTileKind.Add("Foto seitlich"),
            height = 80.dp,
            modifier = Modifier.width(100.667.dp),
        )
    }
    Caption("PhotoTile · Scene ohne Haken, Icon, Add ohne Beschriftung · Höhe 76 (AndAbnMangel:43-49)")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PhotoTile(
            kind = PhotoTileKind.Scene("Foto 1", checked = false),
            height = 76.dp,
            modifier = Modifier.width(76.dp),
        )
        PhotoTile(
            kind = PhotoTileKind.Icon(BaupilotIcon.Image),
            height = 76.dp,
            modifier = Modifier.width(76.dp),
            contentDescription = "Foto 2",
        )
        PhotoTile(
            kind = PhotoTileKind.Add(null),
            height = 76.dp,
            modifier = Modifier.width(76.dp),
            contentDescription = "Foto aufnehmen",
        )
    }
    Caption("WindowLevelSketch · 56 × 44")
    WindowLevelSketch()
}
