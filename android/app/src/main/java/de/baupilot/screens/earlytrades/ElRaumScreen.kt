package de.baupilot.screens.earlytrades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.RoomCheckModel
import de.baupilot.app.SocketStatus
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotLinearProgress
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.BaupilotVerticalStepper
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.SocketTile
import de.baupilot.designsystem.components.SocketTileStyle
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import de.baupilot.designsystem.components.TitleStyle

/** Actions of the room, panel and smoke alarm screens; the shell turns them into navigation. */
sealed interface CheckStepAction {
    data class Open(val route: AppRoute) : CheckStepAction
}

/**
 * Width split of the two buttons (AndElRaum.dc.html:62-63), measured on the reference: both are `flex: 1`, but the
 * `<a>` is content-box (its 1 + 1 border adds to its width) and the `<button>` keeps the browser's padding 6 + 6.
 * With 318 - 8 = 310 shared, each gets 148 plus its own extra: 150 for "Mangel", 160 for the ok button. (The M4 pair
 * in `InspectionStepScreen.kt` is private, so this screen names its own.)
 */
private const val DEFECT_BUTTON_WEIGHT = 150f
private const val OK_BUTTON_WEIGHT = 160f

/** Sockets per row of the grid (`repeat(4, minmax(0, 1fr))`) and the gap between tiles (AndElRaum:47). */
private const val SOCKET_COLUMNS = 4
private val SOCKET_GAP = 8.dp

/**
 * Elektro Raumprüfung Küche (Nr. 21, AndElRaum.dc.html:17-78): close bar with the counter, the fixed 30 % progress
 * bar and the rail stepper. The current step carries the text, the socket tiles, the annotated progress line and
 * the two buttons; "Mangel" opens the defect form, the ok button and the sockets have no action (I6).
 * The tiles are laid out here from [SocketTile] (not `SocketGrid`) because `SocketItem` cannot carry the
 * `room.socket.<id>` test tag; the grid is plain weighted rows, so the stepper's intrinsic measuring still works.
 */
@Composable
fun ElRaumScreen(
    model: RoomCheckModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (CheckStepAction) -> Unit = {},
) {
    val route = model.route
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                subtitle = model.subtitle,
                padding = BarPadding.NavCounter,
                navigation = Nav.Close(onClick = onClose),
                titleStyle = TitleStyle.Detail,
                trailingText = model.counter,
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.inspection.${route.tradeId}.room.${route.roomId}"),
    ) {
        BaupilotLinearProgress(progress = model.progress, modifier = Modifier.padding(horizontal = 16.dp))
        BaupilotVerticalStepper(
            steps = steps(model, onAction),
            variant = StepperVariant.Rail,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        )
    }
}

private fun steps(model: RoomCheckModel, onAction: (CheckStepAction) -> Unit): List<Step> {
    val current = model.current
    val done = model.done.mapIndexed { index, row ->
        Step(
            number = index + 1,
            title = row.label,
            state = StepState.Done,
            meta = row.meta,
            metaColor = Tokens.okFg,
        )
    }
    val upcoming = model.next.mapIndexed { index, row ->
        // `n` is a JSON string; the position after the current step is the fallback for a non-numeric entry.
        Step(number = row.n.toIntOrNull() ?: (current.n + 1 + index), title = row.label, state = StepState.Open)
    }
    val now = Step(
        number = current.n,
        title = current.title,
        state = StepState.Current,
        body = { CurrentBody(model, onAction) },
    )
    return done + now + upcoming
}

@Composable
private fun CurrentBody(model: RoomCheckModel, onAction: (CheckStepAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BaupilotText(text = model.current.text, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
        Column(verticalArrangement = Arrangement.spacedBy(SOCKET_GAP)) {
            model.sockets.chunked(SOCKET_COLUMNS).forEach { rowSockets ->
                Row(horizontalArrangement = Arrangement.spacedBy(SOCKET_GAP)) {
                    rowSockets.forEach { socket ->
                        SocketTile(
                            id = socket.id,
                            label = socket.label,
                            style = socket.status.style(),
                            contentDescription = socket.accessibilityLabel,
                            modifier = Modifier.weight(1f).testTag("room.socket.${socket.id}"),
                        )
                    }
                    repeat(SOCKET_COLUMNS - rowSockets.size) { Spacer(Modifier.weight(1f).height(52.dp)) }
                }
            }
        }
        BaupilotText(text = progressLine(model), size = 13.sp, color = Tokens.muted)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            OutlinedButton(
                label = model.defectLabel,
                modifier = Modifier.weight(DEFECT_BUTTON_WEIGHT).testTag("step.defect"),
                contentColor = Tokens.err,
                borderOutside = true,
                onClick = { onAction(CheckStepAction.Open(model.defectTarget)) },
            )
            FilledButton(
                label = model.okNote,
                size = ButtonSize.Regular,
                container = FilledContainer.Primary,
                modifier = Modifier.weight(OK_BUTTON_WEIGHT).testTag("step.ok"),
            )
        }
    }
}

/** AndElRaum:60: "5 von 8 getestet · " in `muted`, then the defect note in `err` / 500. */
@Composable
private fun progressLine(model: RoomCheckModel): AnnotatedString {
    val defectColor = Tokens.err.color()
    return buildAnnotatedString {
        append("${model.progressLabel} ")
        withStyle(SpanStyle(color = defectColor, fontWeight = FontWeight.Medium)) {
            append(model.defectNote)
        }
    }
}

/** The `K` map of AndElRaum.dc.html:91-96: tile fill, text colour and the `now` tile's 2 dp `primary` border. */
private fun SocketStatus.style(): SocketTileStyle = when (this) {
    SocketStatus.Ok -> SocketTileStyle(Tokens.okBg, Tokens.okFg)
    SocketStatus.Defect -> SocketTileStyle(Tokens.errBg, Tokens.errFg)
    SocketStatus.Now -> SocketTileStyle(Tokens.nowBg, Tokens.nowFg, Tokens.primary)
    SocketStatus.Open -> SocketTileStyle(Tokens.openBg, Tokens.muted)
}
