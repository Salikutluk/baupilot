package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens

/**
 * Switch, 52 x 32, radius 16, 2 border. Custom geometry on purpose: the stock Material 3 Switch
 * is off by 2 dp against the prototype. On: `primary` track and border, 24 `onPrimary` thumb at
 * the end. Off: `offTrack` track, `outline` border, 16 `outline` thumb at the start.
 */
@Composable
fun BaupilotSwitch(
    checked: Boolean,
    contentDescription: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onTrack: ColorToken = Tokens.primary,
    onBorder: ColorToken = Tokens.primary,
    onThumb: ColorToken = Tokens.onPrimary,
    offTrack: ColorToken = Tokens.offTrack,
    offBorder: ColorToken = Tokens.outline,
    offThumb: ColorToken = Tokens.outline,
) {
    baupilotSwitchMeasured(
        checked = checked,
        contentDescription = contentDescription,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        onTrack = onTrack,
        onBorder = onBorder,
        onThumb = onThumb,
        offTrack = offTrack,
        offBorder = offBorder,
        offThumb = offThumb,
    )
}

/** Same switch with a layout hook for the metric tests: [thumbModifier] sits on the thumb. */
@Composable
internal fun baupilotSwitchMeasured(
    checked: Boolean,
    contentDescription: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    thumbModifier: Modifier = Modifier,
    onTrack: ColorToken = Tokens.primary,
    onBorder: ColorToken = Tokens.primary,
    onThumb: ColorToken = Tokens.onPrimary,
    offTrack: ColorToken = Tokens.offTrack,
    offBorder: ColorToken = Tokens.outline,
    offThumb: ColorToken = Tokens.outline,
) {
    val shape = RoundedCornerShape(16.dp)
    val track = if (checked) onTrack else offTrack
    val border = if (checked) onBorder else offBorder
    val thumb = if (checked) onThumb else offThumb
    val thumbSize = if (checked) 24.dp else 16.dp
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .background(track.color(), shape)
            .border(2.dp, border.color(), shape)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .semantics { this.contentDescription = contentDescription }
            // CSS border 2 + padding-x 4: a Compose border draws but does not take up space.
            .padding(horizontal = 6.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(thumbModifier.size(thumbSize).background(thumb.color(), CircleShape))
    }
}
