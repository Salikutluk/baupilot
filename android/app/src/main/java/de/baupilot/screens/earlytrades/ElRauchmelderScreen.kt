package de.baupilot.screens.earlytrades

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.SmokeCheckModel
import de.baupilot.app.SmokeRoom
import de.baupilot.app.SmokeRoomStatus
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotSwitch
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.CheckHintRow
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing

/**
 * Width split of the two buttons (AndElRauchmelder.dc.html:44-45), the same measurement as the panel screen: both
 * are `flex: 1`, the `<a>` is content-box (its 1 + 1 border adds to its width) and the `<button>` keeps the browser's
 * padding 6 + 6. With 358 - 2 x 16 (card padding) = 326 minus the 8 gap = 318 shared and 2 + 12 of own extras, each
 * gets (318 - 14) / 2 = 152 plus its own extra: 154 for "Kein Piepton", 164 for the ok button (D14).
 */
private const val DEFECT_BUTTON_WEIGHT = 154f
private const val OK_BUTTON_WEIGHT = 164f

/**
 * Elektro Rauchwarnmelder (Nr. 23, AndElRauchmelder.dc.html:17-69): back bar with the counter and no progress bar,
 * the subtitle, the current card (check hints, two buttons), the room rows and the reminder row. "Kein Piepton"
 * opens the defect form; the ok button, the room rows and the check rows have no action (I6). The switch keeps its
 * state here, starts on and is saved nowhere (D12).
 */
@Composable
fun ElRauchmelderScreen(
    model: SmokeCheckModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (CheckStepAction) -> Unit = {},
) {
    var reminderOn by rememberSaveable { mutableStateOf(model.reminderInitiallyOn) }
    val route = model.route
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                padding = BarPadding.NavCounter,
                navigation = Nav.Back(onClick = onBack),
                titleStyle = TitleStyle.Detail,
                trailingText = model.counter,
            )
        },
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 0.dp),
        spacing = 14.dp,
        modifier = modifier.testTag("screen.inspection.${route.tradeId}.${route.check}"),
    ) {
        BaupilotText(text = model.subtitle, size = 14.sp, lineHeight = 20.sp, color = Tokens.muted)
        CurrentCard(model, onAction)
        Column(Modifier.fillMaxWidth()) {
            model.rooms.forEachIndexed { index, room -> RoomRow(room, Modifier.testTag("smoke.room.$index")) }
        }
        Column(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.outlineVariant.color()))
            BaupilotListItem(
                title = model.reminder,
                subtitle = model.battery,
                modifier = Modifier.padding(top = 8.dp),
                trailing = Trailing.Switch {
                    BaupilotSwitch(
                        checked = reminderOn,
                        contentDescription = model.reminder,
                        onCheckedChange = { reminderOn = it },
                        modifier = Modifier.testTag("smoke.reminder"),
                    )
                },
                minHeight = ListItemHeight.H56,
                horizontalPadding = 0.dp,
                subtitleSize = 13.sp,
            )
        }
    }
}

/** AndElRauchmelder:28-46: the `primaryContainer` card (gap 12): header, the check hints, the two buttons. */
@Composable
private fun CurrentCard(model: SmokeCheckModel, onAction: (CheckStepAction) -> Unit) {
    BaupilotCard(container = Tokens.primaryContainer) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BaupilotText(
                    text = model.room, size = 20.sp, weight = 500, color = Tokens.onPrimaryContainer,
                    modifier = Modifier.alignByBaseline(),
                )
                BaupilotText(
                    text = model.nowLabel, size = 13.sp, weight = 500, color = Tokens.onPrimaryContainer,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            model.checks.forEachIndexed { index, check ->
                CheckHintRow(
                    title = check.label,
                    hint = check.hint,
                    done = check.done,
                    border = Tokens.onPrimaryContainer,
                    fill = Tokens.onPrimaryContainer,
                    tick = Tokens.primaryContainer,
                    modifier = Modifier.testTag("smoke.check.$index"),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                OutlinedButton(
                    label = model.outcomes[0],
                    modifier = Modifier.weight(DEFECT_BUTTON_WEIGHT).testTag("step.defect"),
                    contentColor = Tokens.onPrimaryContainer,
                    borderColor = Tokens.onPrimaryContainer,
                    borderOutside = true,
                    onClick = { onAction(CheckStepAction.Open(model.defectTarget)) },
                )
                FilledButton(
                    label = model.outcomes[1],
                    size = ButtonSize.Regular,
                    container = FilledContainer.Primary,
                    modifier = Modifier.weight(OK_BUTTON_WEIGHT).testTag("step.ok"),
                )
            }
        }
    }
}

/** AndElRauchmelder:54-60: min 48, dot 10 at 7, name 16, status 13 / 500. The `R` map of the prototype (line 81). */
@Composable
private fun RoomRow(room: SmokeRoom, modifier: Modifier) {
    val (dot, text) = room.status.colors()
    BaupilotListItem(
        title = room.name,
        modifier = modifier,
        leading = Leading.Dot(dot),
        trailing = Trailing.Text(text = room.statusLabel, size = 13.sp, color = text, weight = 500),
        minHeight = ListItemHeight.H48,
        horizontalPadding = 0.dp,
    )
}

/** ok: `ok` dot, `okFg` text; now: `primary` both; open: `outline` dot, `muted` text. */
private fun SmokeRoomStatus.colors(): Pair<ColorToken, ColorToken> = when (this) {
    SmokeRoomStatus.Ok -> Tokens.ok to Tokens.okFg
    SmokeRoomStatus.Now -> Tokens.primary to Tokens.primary
    SmokeRoomStatus.Open -> Tokens.outline to Tokens.muted
}
