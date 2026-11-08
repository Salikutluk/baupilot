package de.baupilot.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BaupilotLinearProgress
import de.baupilot.designsystem.components.BaupilotSearchBar
import de.baupilot.designsystem.components.BaupilotSwitch
import de.baupilot.designsystem.components.BaupilotVerticalStepper
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant

@Composable
internal fun InputsPage() {
    Caption("SearchBar · Platzhalter, Zähler rechts")
    BaupilotSearchBar(query = "", placeholder = "Dokumente durchsuchen", trailingText = "26", onQueryChange = {})
    Caption("Switch · an und aus")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        BaupilotSwitch(checked = true, contentDescription = "Fristen & Termine", onCheckedChange = {})
        BaupilotSwitch(checked = false, contentDescription = "Neue Dokumente", onCheckedChange = {})
    }
    Caption("LinearProgress · 42 %, Standard-Spur")
    BaupilotLinearProgress(progress = 0.42f)
    Caption("LinearProgress · 45 %, track.home auf primaryContainer")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Tokens.primaryContainer.color(), RoundedCornerShape(Tokens.Radius.card))
            .padding(16.dp),
    ) {
        BaupilotLinearProgress(progress = 0.45f, track = Tokens.track.home)
    }
    Caption("VerticalStepper · Rail")
    BaupilotVerticalStepper(
        steps = listOf(
            Step(1, "Fenster", StepState.Done, meta = "ok"),
            Step(2, "Türen", StepState.Current, body = {
                BaupilotText(
                    text = "Wasserwaage oben und seitlich an den Rahmen halten – die Blase muss jeweils mittig stehen.",
                    size = 14.sp,
                    color = Tokens.muted,
                )
            }),
            Step(3, "Boden", StepState.Open),
        ),
        variant = StepperVariant.Rail,
    )
    Caption("VerticalStepper · Compact")
    BaupilotVerticalStepper(
        steps = listOf(
            Step(1, "Fenster", StepState.Done, meta = "ok"),
            Step(2, "Türen", StepState.Current, body = {
                BaupilotText(
                    text = "Wasserwaage oben und seitlich an den Rahmen halten – die Blase muss jeweils mittig stehen.",
                    size = 14.sp,
                    color = Tokens.muted,
                )
            }),
            Step(3, "Boden", StepState.Open, tag = "Mangel"),
        ),
        variant = StepperVariant.Compact,
    )
}

private const val ICON_COLUMNS = 5

@Composable
internal fun IconsPage() {
    BaupilotIcon.entries.chunked(ICON_COLUMNS).forEach { row ->
        Row(Modifier.fillMaxWidth()) {
            row.forEach { icon ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(icon, 24.dp, Tokens.text.color())
                    BaupilotText(
                        text = icon.name,
                        size = 9.sp,
                        color = Tokens.muted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            repeat(ICON_COLUMNS - row.size) { Column(Modifier.weight(1f)) {} }
        }
    }
}
