package de.baupilot.screens.earlytrades

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.BannerTone
import de.baupilot.app.NavigationTarget
import de.baupilot.app.ProtocolBanner as ProtocolBannerModel
import de.baupilot.app.RowMark
import de.baupilot.app.TradeProtocolModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BannerTrailing
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.ProtocolBanner
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing

/** Actions of the protocol screens (Erdarbeiten, Rohbau, Zimmerer); the shell turns them into navigation (D2). */
sealed interface ProtocolAction {
    data class Open(val target: NavigationTarget) : ProtocolAction
}

/**
 * Shared body of the protocol screens (AndErd:17-48, AndRohbau): detail top bar, the tone banner and the section
 * headers with their rows. The banner follows `model.banner.tone`: `Ok` carries the PDF button (no action), `Warn`
 * a chevron and the whole banner opens the banner target. A banner or row target of `null` means no click handler.
 */
@Composable
internal fun ProtocolTemplate(
    model: TradeProtocolModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (ProtocolAction) -> Unit = {},
) {
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
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.inspection.${model.tradeId}"),
    ) {
        Banner(model.banner, onAction)
        model.groups.forEachIndexed { groupIndex, group ->
            SectionHeader(
                title = group.title,
                style = SectionHeaderStyle.Compact(),
                modifier = Modifier.padding(PaddingValues(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 2.dp)),
            )
            group.rows.forEachIndexed { rowIndex, row ->
                val target = row.target
                val defect = row.mark == RowMark.Defect
                BaupilotListItem(
                    title = row.label,
                    modifier = Modifier.testTag("protocol.row.$groupIndex.$rowIndex"),
                    leading = Leading.Icon(
                        if (defect) BaupilotIcon.AlertCircleW24 else BaupilotIcon.CheckCircleW24,
                        if (defect) Tokens.err else Tokens.ok,
                    ),
                    trailing = Trailing.Text(
                        text = row.meta,
                        size = 13.sp,
                        color = if (defect) Tokens.err else Tokens.muted,
                        weight = if (defect) 500 else 400,
                    ),
                    minHeight = ListItemHeight.H48,
                    onClick = target?.let { { onAction(ProtocolAction.Open(it)) } },
                    titleSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun Banner(banner: ProtocolBannerModel, onAction: (ProtocolAction) -> Unit) {
    val ok = banner.tone == BannerTone.Ok
    val target = banner.target
    ProtocolBanner(
        icon = if (ok) BaupilotIcon.CheckCircleW24 else BaupilotIcon.AlertTriangleW22,
        title = banner.title,
        text = banner.text,
        background = if (ok) Tokens.okBg else Tokens.warnBg,
        foreground = if (ok) Tokens.okFg else Tokens.warnFg,
        modifier = Modifier.padding(PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp)).testTag("protocol.banner"),
        trailing = when {
            !ok -> BannerTrailing.Chevron
            banner.documentLabel != null -> BannerTrailing.Document(banner.documentLabel)
            else -> BannerTrailing.None
        },
        onClick = if (ok) null else target?.let { { onAction(ProtocolAction.Open(it)) } },
    )
}

/**
 * Shared body of the Rohbau and Zimmerer stubs until their screens use [ProtocolTemplate]. A `null` target means no
 * click handler at all; the PDF button (`protocol.document`) is a placeholder without an action.
 */
@Composable
internal fun ProtocolStub(
    model: TradeProtocolModel,
    onBack: () -> Unit,
    modifier: Modifier,
    onAction: (ProtocolAction) -> Unit,
) {
    Column(modifier.fillMaxSize().testTag("screen.inspection.${model.tradeId}")) {
        BaupilotTopAppBar(
            title = model.title,
            subtitle = model.subtitle,
            padding = BarPadding.Nav,
            navigation = Nav.Back(onClick = onBack),
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            val banner = model.banner
            val bannerTarget = banner.target
            Column(
                Modifier
                    .fillMaxWidth()
                    .testTag("protocol.banner")
                    .then(if (bannerTarget != null) Modifier.clickable { onAction(ProtocolAction.Open(bannerTarget)) } else Modifier),
            ) {
                BasicText(banner.title)
                BasicText(banner.text)
            }
            banner.documentLabel?.let { BasicText(it, Modifier.testTag("protocol.document")) }
            model.groups.forEachIndexed { groupIndex, group ->
                BasicText(group.title)
                group.rows.forEachIndexed { rowIndex, row ->
                    val target = row.target
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("protocol.row.$groupIndex.$rowIndex")
                            .then(if (target != null) Modifier.clickable { onAction(ProtocolAction.Open(target)) } else Modifier),
                    ) {
                        BasicText(row.label)
                        BasicText(row.meta)
                    }
                }
            }
        }
    }
}
