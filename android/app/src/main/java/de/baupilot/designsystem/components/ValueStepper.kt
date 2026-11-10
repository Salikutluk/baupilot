package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

/** Test tags of a [ValueStepper]'s three parts. */
data class StepperTags(val decrement: String, val value: String, val increment: String)

/**
 * Minus / value / plus stepper (AndRohbauMasse:38-42): 48 inner + 1 `outline` border (50 tall, 150 wide with the
 * default [valueMinWidth]), radius 24, padding 0 4, gap 4. The 40 dp buttons are `secondaryContainer` circles with a
 * 20 glyph; the value is 16 / 500.
 *
 * The buttons stay 40 dp in layout. Compose widens every pointer-input node's hit area to the view configuration's
 * minimum touch target (48 dp) without changing its size, so the touch target is ≥ 48 and the layout is untouched.
 */
@Composable
fun ValueStepper(
    label: String,
    value: String,
    onDecrement: (() -> Unit)?,
    onIncrement: (() -> Unit)?,
    modifier: Modifier = Modifier,
    valueMinWidth: Dp = 52.dp,
    tags: StepperTags? = null,
) {
    Row(
        modifier = modifier
            .height(50.dp)
            .border(1.dp, Tokens.outline.color(), RoundedCornerShape(24.dp))
            .padding(1.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        StepperButton("−", "$label verringern", onDecrement, tags?.decrement)
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = valueMinWidth)
                .then(if (tags != null) Modifier.testTag(tags.value) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            BaupilotText(text = value, size = 16.sp, weight = 500, color = Tokens.text)
        }
        StepperButton("+", "$label erhöhen", onIncrement, tags?.increment)
    }
}

@Composable
private fun StepperButton(glyph: String, description: String, onClick: (() -> Unit)?, tag: String?) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Tokens.secondaryContainer.color())
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .semantics { contentDescription = description }
            .then(if (tag != null) Modifier.testTag(tag) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        BaupilotText(
            text = glyph,
            size = 20.sp,
            color = Tokens.onSecondaryContainer,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}
