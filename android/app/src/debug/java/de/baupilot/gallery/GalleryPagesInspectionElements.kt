package de.baupilot.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.ExtendedFab
import de.baupilot.designsystem.components.FabIcon
import de.baupilot.designsystem.components.SegmentedProgress

@Composable
internal fun InspectionElementsPage() {
    val ok = Tokens.ok
    val defect = Tokens.err
    val open = Tokens.track.abnElemente
    Caption("SegmentedProgress · Höhe 6, 12 Segmente, Lücke 3, Radius 3 (AndAbnElemente:23)")
    SegmentedProgress(
        segments = listOf(ok, defect, ok, ok, ok, ok, ok, open, open, open, open, open),
        height = 6.dp,
        contentDescription = "7 von 12 geprüft",
    )
    Caption("SegmentedProgress · 3 Segmente")
    SegmentedProgress(
        segments = listOf(ok, defect, open),
        height = 6.dp,
        contentDescription = "1 von 3 geprüft",
    )
    Caption("DetailScreenScaffold · FAB-Slot, Ende 16, unten 24 (AndAbnElemente:64)")
    Box(Modifier.fillMaxWidth().height(160.dp)) {
        DetailScreenScaffold(
            topBar = {},
            contentPadding = PaddingValues(0.dp),
            spacing = 0.dp,
            fab = { ExtendedFab(label = "Weiter: F8 · Schlafen", icon = FabIcon.Play) },
            content = {},
        )
    }
}
