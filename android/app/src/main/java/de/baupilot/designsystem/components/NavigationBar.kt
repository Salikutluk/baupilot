package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/** The five top-level destinations of the bottom navigation bar. */
enum class Tab(val icon: BaupilotIcon, val label: String) {
    Overview(BaupilotIcon.Home, "Übersicht"),
    Schedule(BaupilotIcon.Calendar, "Zeitplan"),
    Acceptance(BaupilotIcon.ClipboardCheck, "Abnahmen"),
    Documents(BaupilotIcon.Folder, "Dokumente"),
    Profile(BaupilotIcon.User, "Profil"),
}

/** Bottom navigation: 80 high, five equal columns, 56 x 32 pill behind the active icon. */
@Composable
fun BaupilotNavigationBar(
    selected: Tab,
    onSelect: (Tab) -> Unit,
    background: ColorToken = Tokens.surfaceContainer,
    pill: ColorToken = Tokens.secondaryContainer,
    activeColor: ColorToken = Tokens.text,
    inactiveColor: ColorToken = Tokens.muted,
) {
    baupilotNavigationBarMeasured(
        selected = selected,
        onSelect = onSelect,
        background = background,
        pill = pill,
        activeColor = activeColor,
        inactiveColor = inactiveColor,
    )
}

/** Same bar with layout hooks for the metric tests: [modifier] on the bar, [activePillModifier] on the pill. */
@Composable
internal fun baupilotNavigationBarMeasured(
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
    activePillModifier: Modifier = Modifier,
    background: ColorToken = Tokens.surfaceContainer,
    pill: ColorToken = Tokens.secondaryContainer,
    activeColor: ColorToken = Tokens.text,
    inactiveColor: ColorToken = Tokens.muted,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Tokens.Layout.navigationBar)
            .background(background.color())
            .padding(top = 12.dp)
            .semantics { contentDescription = "Hauptnavigation" },
    ) {
        Tab.entries.forEach { tab ->
            val active = tab == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(tab) }
                    .semantics { this.selected = active },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = (if (active) activePillModifier else Modifier)
                        .size(Tokens.Layout.navPill)
                        .background(
                            if (active) pill.color() else Color.Transparent,
                            RoundedCornerShape(16.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon = tab.icon,
                        size = 24.dp,
                        tint = (if (active) activeColor else inactiveColor).color(),
                    )
                }
                BaupilotText(
                    text = tab.label,
                    size = 12.sp,
                    weight = if (active) 700 else 500,
                    color = if (active) activeColor else inactiveColor,
                )
            }
        }
    }
}
