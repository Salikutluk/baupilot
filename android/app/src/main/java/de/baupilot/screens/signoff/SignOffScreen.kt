package de.baupilot.screens.signoff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.CountTone
import de.baupilot.app.NavigationTarget
import de.baupilot.app.SignOffModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.CaptionedActionBar
import de.baupilot.designsystem.components.CountTile
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.SignatureCard
import de.baupilot.designsystem.components.SignatureCardStyle
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing
import de.baupilot.inspection.Extent
import de.baupilot.inspection.Point
import de.baupilot.inspection.SignatureStrokes

/** Actions the Abnahme screen raises; the shell turns them into navigation. */
sealed interface SignOffAction {
    data class Finish(val target: NavigationTarget) : SignOffAction
}

/** Fill and ink of a count tile (renderVals of AndAbnSignatur.dc.html:85-88). */
private fun CountTone.colors(): Pair<ColorToken, ColorToken> = when (this) {
    CountTone.Surface -> Tokens.surfaceContainerHigh to Tokens.text
    CountTone.Ok -> Tokens.okBg to Tokens.okFg
    CountTone.Error -> Tokens.errBg to Tokens.errFg
}

/**
 * Saves the owner's strokes as `[[width, height], [x0, y0, x1, y1, …], …]` so they survive rotation and the shell's
 * saveable entry (D10). A drawing in progress is never saved: the restored value is always closed.
 */
internal val SignatureStrokesSaver: Saver<SignatureStrokes, Any> = Saver(
    save = { value ->
        listOf(listOf(value.bounds.width, value.bounds.height)) +
            value.strokes.map { stroke -> stroke.flatMap { listOf(it.x, it.y) } }
    },
    restore = { saved ->
        @Suppress("UNCHECKED_CAST")
        val lists = saved as List<List<Float>>
        SignatureStrokes(
            bounds = Extent(lists[0][0], lists[0][1]),
            strokes = lists.drop(1).map { flat -> flat.chunked(2).map { Point(it[0], it[1]) } },
        )
    },
)

/**
 * Abnahme & Unterschrift (Nr. 14, AndAbnSignatur.dc.html): detail bar with the subtitle, the three count tiles, the
 * reserved defects with the reservation text, the craftsman's signed tile and the owner's drawing surface, and the
 * pinned bar with "Protokoll abschließen". The owner's strokes are local state (D10); nothing is written anywhere (I8).
 */
@Composable
fun SignOffScreen(
    model: SignOffModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (SignOffAction) -> Unit = {},
) {
    var strokes by rememberSaveable(stateSaver = SignatureStrokesSaver) {
        mutableStateOf(SignatureStrokes(Extent(0f, 0f)))
    }

    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                subtitle = model.subtitle,
                padding = BarPadding.Nav,
                navigation = Nav.Back(onClick = onBack),
                titleStyle = TitleStyle.Detail,
            )
        },
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
        spacing = 14.dp,
        modifier = modifier.testTag("screen.inspection.${model.inspectionId}.sign"),
        bottomBar = {
            CaptionedActionBar(caption = model.note) {
                FilledButton(
                    label = model.finishLabel,
                    size = ButtonSize.Regular,
                    container = FilledContainer.Primary,
                    modifier = Modifier.testTag("sign.finish"),
                    onClick = { onAction(SignOffAction.Finish(model.finishTarget)) },
                )
            }
        },
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            model.stats.forEach { stat ->
                val (container, content) = stat.tone.colors()
                CountTile(
                    value = stat.value,
                    label = stat.label,
                    container = container,
                    content = content,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Column {
            SectionHeader(
                title = model.reservedHeading,
                style = SectionHeaderStyle.Compact(),
                modifier = Modifier.padding(vertical = 4.dp),
            )
            model.defects.forEach { defect ->
                BaupilotListItem(
                    title = defect.title,
                    subtitle = defect.meta,
                    leading = Leading.Icon(BaupilotIcon.AlertTriangleW22, Tokens.err),
                    trailing = Trailing.Text(defect.due, 13.sp),
                    minHeight = ListItemHeight.H56,
                    horizontalPadding = 0.dp,
                )
            }
            BaupilotText(
                text = model.reservation,
                size = 13.sp,
                color = Tokens.muted,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SignatureCard(
                SignatureCardStyle.Signed(
                    label = model.craftsmanLabel,
                    name = model.craftsmanName,
                    status = model.craftsmanStatus,
                    signatureDescription = model.signatureLabel,
                ),
                Modifier.testTag("sign.craftsman"),
            )
            SignatureCard(
                SignatureCardStyle.Pending(
                    caption = model.ownerLabel,
                    placeholder = model.signHereLabel,
                    strokes = strokes,
                    onStrokesChange = { strokes = it },
                ),
                Modifier.testTag("sign.owner"),
            )
        }
    }
}
