package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

enum class StepState { Done, Current, Open }

enum class StepperVariant { Rail, Compact }

/** One stepper entry. [body] is only drawn for the current step; [trailing] ends a compact done or open row (the MaRaum colour swatch). */
data class Step(
    val number: Int,
    val title: String,
    val state: StepState,
    val meta: String? = null,
    val tag: String? = null,
    val metaColor: ColorToken = Tokens.muted,
    val metaWeight: Int = 400,
    val body: (@Composable () -> Unit)? = null,
    val trailing: (@Composable () -> Unit)? = null,
)

/**
 * Vertical stepper. Rail (AndAbnPruefung): numbered circles joined by a 1 `outlineVariant` line.
 * Compact (AndElVerteiler): no line; the current step sits in a `surfaceContainer` card whose rows are
 * [currentSpacing] apart (SanDruck: 14), and open rows are at least [openMinHeight] tall.
 */
@Composable
fun BaupilotVerticalStepper(
    steps: List<Step>,
    variant: StepperVariant,
    modifier: Modifier = Modifier,
    lineColor: ColorToken = Tokens.outlineVariant,
    tagColor: ColorToken = Tokens.err,
    currentSpacing: Dp = 12.dp,
    openMinHeight: Dp = 44.dp,
) {
    baupilotVerticalStepperMeasured(
        steps = steps,
        variant = variant,
        modifier = modifier,
        lineColor = lineColor,
        tagColor = tagColor,
        currentSpacing = currentSpacing,
        openMinHeight = openMinHeight,
    )
}

/** Same stepper with a layout hook for the metric tests: [circleModifier] sits on every circle. */
@Composable
internal fun baupilotVerticalStepperMeasured(
    steps: List<Step>,
    variant: StepperVariant,
    modifier: Modifier = Modifier,
    circleModifier: Modifier = Modifier,
    lineColor: ColorToken = Tokens.outlineVariant,
    tagColor: ColorToken = Tokens.err,
    currentSpacing: Dp = 12.dp,
    openMinHeight: Dp = 44.dp,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            when (variant) {
                StepperVariant.Rail -> RailStep(step, isLast = index == steps.lastIndex, circleModifier, lineColor)
                StepperVariant.Compact -> CompactStep(step, circleModifier, tagColor, currentSpacing, openMinHeight)
            }
        }
    }
}

/** 24 dp circle: filled `primary` with a check or number (done, current) or a 1 `outline` ring (open). */
@Composable
private fun StepCircle(step: Step, modifier: Modifier) {
    val filled = step.state != StepState.Open
    var frame = modifier.size(24.dp)
    frame = if (filled) {
        frame.background(Tokens.primary.color(), CircleShape)
    } else {
        frame.border(1.dp, Tokens.outline.color(), CircleShape)
    }
    Box(modifier = frame, contentAlignment = Alignment.Center) {
        when {
            step.state == StepState.Done -> Icon(BaupilotIcon.CheckW32, 14.dp, Tokens.onPrimary.color())
            step.state == StepState.Current ->
                BaupilotText(text = step.number.toString(), size = 13.sp, weight = 500, color = Tokens.onPrimary)
            else -> BaupilotText(text = step.number.toString(), size = 12.sp, weight = 500, color = Tokens.muted)
        }
    }
}

@Composable
private fun RailStep(step: Step, isLast: Boolean, circleModifier: Modifier, lineColor: ColorToken) {
    val minHeight = when (step.state) {
        StepState.Done -> 52.dp
        StepState.Open -> 48.dp
        StepState.Current -> 0.dp
    }
    Row(
        modifier = Modifier.heightIn(min = minHeight).height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.width(24.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            StepCircle(step, circleModifier)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(1.dp)
                    .background(if (isLast) Color.Transparent else lineColor.color()),
            )
        }
        when (step.state) {
            StepState.Done -> Column(
                modifier = Modifier.padding(top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                BaupilotText(text = step.title, size = 15.sp, color = Tokens.text)
                step.meta?.let { BaupilotText(text = it, size = 13.sp, weight = step.metaWeight, color = step.metaColor) }
            }
            StepState.Current -> Column(
                modifier = Modifier.weight(1f).padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BaupilotText(
                    text = step.title,
                    size = 18.sp,
                    weight = 500,
                    color = Tokens.text,
                    modifier = Modifier.padding(top = 2.dp),
                )
                step.body?.invoke()
            }
            StepState.Open -> BaupilotText(
                text = step.title,
                size = 15.sp,
                color = Tokens.muted,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun CompactStep(
    step: Step,
    circleModifier: Modifier,
    tagColor: ColorToken,
    currentSpacing: Dp,
    openMinHeight: Dp,
) {
    if (step.state == StepState.Current) {
        BaupilotCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(currentSpacing)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StepCircle(step, circleModifier)
                    BaupilotText(text = step.title, size = 18.sp, weight = 500, color = Tokens.text)
                }
                step.body?.invoke()
            }
        }
        return
    }
    val minHeight = if (step.state == StepState.Open) openMinHeight else 44.dp
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = minHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StepCircle(step, circleModifier)
        BaupilotText(
            text = step.title,
            size = 15.sp,
            color = if (step.state == StepState.Open) Tokens.muted else Tokens.text,
            modifier = Modifier.weight(1f),
        )
        step.meta?.let { BaupilotText(text = it, size = 13.sp, weight = step.metaWeight, color = step.metaColor) }
        step.tag?.let { BaupilotText(text = it, size = 12.sp, weight = 500, color = tagColor) }
        step.trailing?.invoke()
    }
}
