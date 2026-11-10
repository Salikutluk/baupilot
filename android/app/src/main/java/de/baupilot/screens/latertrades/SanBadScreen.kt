package de.baupilot.screens.latertrades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.ObjectCheckModel
import de.baupilot.app.ObjectState
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.ObjectCheckRow
import de.baupilot.designsystem.components.ObjectCheckTags
import de.baupilot.designsystem.components.ObjectChip
import de.baupilot.designsystem.components.ObjectChipState
import de.baupilot.designsystem.components.TitleStyle

/**
 * Sanitaer Bad OG, Objekte (AndSanBad.dc.html:17-64): the bar with the object counter under the title, the object tabs
 * (a wrapping row of chips), the card of the selected object with its check rows and the note about the next object.
 * The tabs and the defect / ok buttons have no action (D12). [onClose] is the back arrow.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SanBadScreen(
    model: ObjectCheckModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val route = model.route
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                subtitle = model.counter,
                padding = BarPadding.NavCounter,
                navigation = Nav.Back(onClick = onClose),
                titleStyle = TitleStyle.Detail,
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.inspection.${route.tradeId}.room.${route.roomId}"),
    ) {
        FlowRow(
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                .semantics { contentDescription = model.tabsLabel },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            model.objects.forEachIndexed { index, tab ->
                ObjectChip(
                    label = tab.name,
                    state = tab.state.chipState(),
                    modifier = Modifier.testTag("object.tab.$index"),
                )
            }
        }
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BaupilotCard(padding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                BaupilotText(
                    text = model.heading,
                    size = 18.sp,
                    weight = 500,
                    color = Tokens.text,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
                model.checks.forEachIndexed { index, check ->
                    ObjectCheckRow(
                        title = check.label,
                        hint = check.hint,
                        ok = check.ok,
                        showsDivider = index > 0,
                        tags = ObjectCheckTags(ok = "object.check.$index.ok", defect = "object.check.$index.defect"),
                    )
                }
            }
            InfoCallout(
                text = model.nextHint,
                container = null,
                content = Tokens.muted,
                contentPadding = PaddingValues(4.dp),
                textSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

private fun ObjectState.chipState(): ObjectChipState = when (this) {
    ObjectState.Done -> ObjectChipState.Done
    ObjectState.Now -> ObjectChipState.Now
    ObjectState.Open -> ObjectChipState.Open
}
