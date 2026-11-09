package de.baupilot.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

/** Link at the end of an Overview header; a null [onClick] draws it and does nothing. */
class SectionLink(val label: String, val onClick: (() -> Unit)? = null)

/** Look of a [SectionHeader]. */
sealed interface SectionHeaderStyle {
    /** 16 / 500 `primary`; height 40 with a [link], else 32 (AndHome:42, :58). */
    class Overview(val link: SectionLink? = null) : SectionHeaderStyle

    /** 14 / 500 `primary`, padding 12 16 4 (AndProfil:33). */
    object Settings : SectionHeaderStyle

    /** 14 / 500 `primary` with an optional 48 high link, padding 0 12, hanging 12 over the end (AndDokumente:40-43). */
    class Compact(val link: SectionLink? = null) : SectionHeaderStyle
}

/** Section heading in `primary`; [style] sets size, height, padding and the optional trailing link. */
@Composable
fun SectionHeader(title: String, style: SectionHeaderStyle, modifier: Modifier = Modifier) {
    when (style) {
        is SectionHeaderStyle.Overview -> Row(
            modifier = modifier.fillMaxWidth().height(if (style.link != null) 40.dp else 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BaupilotText(
                text = title,
                size = 16.sp,
                weight = 500,
                color = Tokens.primary,
            )
            if (style.link != null) {
                TextButton(
                    label = style.link.label,
                    size = TextButtonSize.Compact40,
                    onClick = style.link.onClick,
                    modifier = Modifier.hangOverEnd(12.dp),
                )
            }
        }

        is SectionHeaderStyle.Compact -> Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BaupilotText(
                text = title,
                size = 14.sp,
                weight = 500,
                color = Tokens.primary,
            )
            if (style.link != null) {
                TextButton(
                    label = style.link.label,
                    size = TextButtonSize.Default,
                    onClick = style.link.onClick,
                    modifier = Modifier.hangOverEnd(12.dp),
                )
            }
        }

        SectionHeaderStyle.Settings -> BaupilotText(
            text = title,
            size = 14.sp,
            weight = 500,
            color = Tokens.primary,
            modifier = modifier.padding(PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 4.dp)),
        )
    }
}

/** CSS negative end margin: the link's own padding hangs over the content edge (AndHome:45, AndDokumente:42). */
private fun Modifier.hangOverEnd(overhang: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width - overhang.roundToPx(), placeable.height) { placeable.placeRelative(0, 0) }
}
