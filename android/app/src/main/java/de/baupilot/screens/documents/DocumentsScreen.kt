package de.baupilot.screens.documents

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.AppRoute
import de.baupilot.app.DocumentKind
import de.baupilot.app.DocumentRow
import de.baupilot.app.DocumentsModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotSearchBar
import de.baupilot.designsystem.components.ExtendedFab
import de.baupilot.designsystem.components.FabIcon
import de.baupilot.designsystem.components.FilterChip
import de.baupilot.designsystem.components.HintCard
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.SectionLink
import de.baupilot.designsystem.components.TabScreenScaffold
import de.baupilot.designsystem.components.Trailing

/**
 * Dokumente (AndDokumente.dc.html): search bar, filter chips, the invoice hint card, the "Zuletzt hinzugefügt"
 * header and the document rows. The search, the hint card, "Nach Gewerk" and the FAB are placeholders without
 * action. The chip selection and the query survive recomposition and rotation; the rows follow
 * `model.rows(filter)`. A row with a target calls [onOpen].
 */
@Composable
fun DocumentsScreen(model: DocumentsModel, modifier: Modifier = Modifier, onOpen: (AppRoute) -> Unit = {}) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(model.initialFilter) }

    TabScreenScaffold(
        topBar = { Spacer(Modifier.height(32.dp)) },
        spacing = 12.dp,
        modifier = modifier.testTag("screen.documents"),
        fab = { ExtendedFab(label = model.uploadLabel, icon = FabIcon.Plus, onClick = null) },
    ) {
        BaupilotSearchBar(
            query = query,
            placeholder = model.searchLabel,
            trailingText = model.countBadge,
            onQueryChange = { query = it },
        )
        Box(Modifier.bleedHorizontally(16.dp)) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .semantics { contentDescription = model.filtersLabel },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                model.filters.forEachIndexed { index, chip ->
                    FilterChip(
                        label = chip.text,
                        selected = index == filter,
                        onClick = { filter = index },
                        modifier = Modifier.testTag("documents.filter.${chip.kind?.key ?: "all"}"),
                    )
                }
            }
        }
        HintCard(
            icon = BaupilotIcon.Clock,
            title = model.banner.title,
            subtitle = model.banner.sub,
            amount = model.banner.amount,
        )
        // AndDokumente:40: margin-bottom -8 on the header, and the list is a sibling of this padded block, not a
        // child of its 12 gap, so the list starts 8 above the header's bottom edge.
        SectionHeader(
            title = model.recentLabel,
            style = SectionHeaderStyle.Compact(SectionLink(model.byTradeLabel)),
            modifier = Modifier.trimBottom(8.dp + 12.dp),
        )
        Column(Modifier.bleedHorizontally(16.dp)) {
            model.rows(filter).forEach { row -> DocumentListRow(row, onOpen) }
        }
    }
}

@Composable
private fun DocumentListRow(row: DocumentRow, onOpen: (AppRoute) -> Unit) {
    val style = row.kind.style()
    val target = row.target
    BaupilotListItem(
        title = row.title,
        subtitle = row.meta,
        leading = Leading.Avatar.icon(style.icon, style.background, style.foreground),
        trailing = row.amount?.takeIf { it.isNotEmpty() }?.let {
            Trailing.Text(it, 14.sp, if (row.kind == DocumentKind.Invoice) Tokens.primary else Tokens.text)
        },
        minHeight = ListItemHeight.H72,
        ellipsis = true,
        onClick = if (target != null) ({ onOpen(target) }) else null,
        modifier = Modifier.testTag("documents.row.${row.id}"),
    )
}

private class KindStyle(val icon: BaupilotIcon, val background: ColorToken, val foreground: ColorToken)

/** Icon and tones by kind (AndDokumente:73-76): Angebot info, Vertrag ok, Rechnung secondary, Plan pl. */
private fun DocumentKind.style(): KindStyle = when (this) {
    DocumentKind.Offer -> KindStyle(BaupilotIcon.Document, Tokens.infoBg, Tokens.infoFg)
    DocumentKind.Contract -> KindStyle(BaupilotIcon.Contract, Tokens.okBg, Tokens.okFg)
    DocumentKind.Invoice -> KindStyle(BaupilotIcon.Receipt, Tokens.secondaryContainer, Tokens.onSecondaryContainer)
    DocumentKind.Plan -> KindStyle(BaupilotIcon.Map, Tokens.plBg, Tokens.plFg)
}

/** CSS `margin: 0 -[amount]`: measured [amount] wider on each side, same layout width. */
private fun Modifier.bleedHorizontally(amount: Dp): Modifier = layout { measurable, constraints ->
    val bleed = amount.roundToPx()
    val width = constraints.maxWidth + 2 * bleed
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) { placeable.placeRelative(-bleed, 0) }
}

/** Reports [amount] less height than measured: a negative bottom margin. */
private fun Modifier.trimBottom(amount: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height - amount.roundToPx()) { placeable.placeRelative(0, 0) }
}
