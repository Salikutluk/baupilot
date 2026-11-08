package de.baupilot.screens.notifications

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.baupilot.app.NavigationTarget
import de.baupilot.app.NotificationsModel
import de.baupilot.designsystem.components.Action
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.NotificationListItem
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.screens.ScreenCopy

/** Actions the notifications raise; the shell turns them into navigation. */
sealed interface NotificationsAction {
    data class Open(val target: NavigationTarget) : NotificationsAction
}

/**
 * Mitteilungen (AndMitteilungen.dc.html): back bar with the "Alle gelesen" placeholder, then the groups with
 * full-width notification rows. A row with a target opens it through [onAction]; a row without one does nothing.
 * Every row carries `notification.<id>` as its test tag.
 */
@Composable
fun NotificationsScreen(
    model: NotificationsModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (NotificationsAction) -> Unit = {},
) {
    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                padding = BarPadding.Nav,
                navigation = Nav.Back(onClick = onBack),
                titleStyle = TitleStyle.Large,
                actions = listOf(Action.Text(model.markAllLabel, height = 48)),
            )
        },
        contentPadding = PaddingValues(0.dp),
        spacing = 0.dp,
        modifier = modifier.testTag("screen.notifications"),
    ) {
        model.groups.forEach { group ->
            SectionHeader(group.title, SectionHeaderStyle.Settings)
            group.items.forEach { item ->
                val style = ScreenCopy.Mitteilungen.kindStyle(item.kind)
                val target = item.target
                NotificationListItem(
                    icon = style.icon,
                    iconBackground = style.background,
                    iconForeground = style.foreground,
                    title = item.title,
                    whenLabel = item.`when`,
                    text = item.text,
                    unread = item.unread,
                    onClick = target?.let { { onAction(NotificationsAction.Open(it)) } },
                    modifier = Modifier.testTag("notification.${item.id}"),
                )
            }
        }
    }
}
