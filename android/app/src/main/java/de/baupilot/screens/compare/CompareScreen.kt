package de.baupilot.screens.compare

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.baupilot.app.AppRoute
import de.baupilot.app.CompareModel
import de.baupilot.compare.columns
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.ActionRow
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.BottomActionBar
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.ComparisonColumn
import de.baupilot.designsystem.components.ComparisonRow
import de.baupilot.designsystem.components.ComparisonTable
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.screens.ScreenCopy

/**
 * Flex shares of the two bottom buttons (1 : 1.4). Chrome shares the 346 dp row as 145.33 and 200.67 dp and paints
 * the seam at 161.33 as 161, so the buttons are 145 and 201 dp wide.
 */
private const val ASK_WEIGHT = 145f
private const val AWARD_WEIGHT = 201f

/** Actions the comparison raises; the shell turns them into navigation. */
sealed interface CompareAction {
    data class OpenDocument(val id: String) : CompareAction
}

/**
 * Angebote vergleichen (AndVergleich.dc.html): back bar with the trade line as subtitle, the comparison table,
 * the summary callout and the pending offer, with "Rückfrage" and the award button pinned to the bottom.
 * A column header whose offer has a document opens it through [onAction]; the other headers, the pending
 * offer's reminder and both bottom buttons are placeholders without action.
 */
@Composable
fun CompareScreen(
    model: CompareModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (CompareAction) -> Unit = {},
) {
    val widths = columns(358f, 16f, 10f, 104f)
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                padding = BarPadding.Nav,
                navigation = Nav.Back(onClick = onBack),
                subtitle = model.subtitle,
                titleStyle = TitleStyle.Detail,
            )
        },
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp),
        spacing = 12.dp,
        modifier = modifier.testTag("screen.compare.${model.tradeId}"),
        bottomBar = {
            BottomActionBar {
                OutlinedButton(
                    label = model.askLabel,
                    modifier = Modifier.weight(ASK_WEIGHT),
                    contentColor = Tokens.primary,
                    borderOutside = true,
                )
                FilledButton(
                    label = model.awardLabel,
                    size = ButtonSize.Regular,
                    container = FilledContainer.Primary,
                    modifier = Modifier.weight(AWARD_WEIGHT).align(Alignment.Top),
                )
            }
        },
    ) {
        ComparisonTable(
            columns = model.columns.map { column ->
                val document = column.target as? AppRoute.Document
                ComparisonColumn(
                    name = column.name,
                    badge = if (column.best) ScreenCopy.Gewerk.BEST else null,
                    onClick = document?.let { target -> { onAction(CompareAction.OpenDocument(target.id)) } },
                    tag = "compare.column.${column.offerId}",
                )
            },
            rows = model.rows.map { ComparisonRow(it.label, it.a, it.b, it.strong, it.highlighted) },
            widths = widths,
            contentDescription = model.tableLabel,
        )
        InfoCallout(model.note)
        model.pending?.let { ActionRow(it.firm, it.text, model.remindLabel) }
    }
}
