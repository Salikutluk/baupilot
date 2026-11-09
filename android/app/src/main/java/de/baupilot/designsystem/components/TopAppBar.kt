package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.wholeDpCentered

/** Leading navigation button of the bar. A null [onClick] draws the button and does nothing. */
sealed interface Nav {
    val onClick: (() -> Unit)?
    val label: String

    data class Back(override val onClick: (() -> Unit)? = null) : Nav {
        override val label = "Zurück"
    }

    /** [label] is "Schließen" or "Abbrechen". */
    data class Close(
        override val label: String = "Schließen",
        override val onClick: (() -> Unit)? = null,
    ) : Nav
}

/** Title typography of the bar. */
sealed interface TitleStyle {
    val size: TextUnit
    val lineHeight: TextUnit

    /** 22 / 400. */
    object Large : TitleStyle {
        override val size = 22.sp
        override val lineHeight = TextUnit.Unspecified
    }

    /** 22 / 400 with line height 28 (Home, Zeitplan). */
    object LargeTall : TitleStyle {
        override val size = 22.sp
        override val lineHeight = 28.sp
    }

    /** 20 with a 13 `muted` subtitle. */
    object Detail : TitleStyle {
        override val size = 20.sp
        override val lineHeight = TextUnit.Unspecified
    }

    /** 18, one line, ellipsis. */
    object Compact : TitleStyle {
        override val size = 18.sp
        override val lineHeight = TextUnit.Unspecified
    }
}

/** Trailing action of the bar. A null [onClick] draws the action and does nothing. */
sealed interface Action {
    val onClick: (() -> Unit)?

    /** 48 round button, icon 22 `muted`. */
    data class Icon(
        val icon: BaupilotIcon,
        val label: String,
        override val onClick: (() -> Unit)? = null,
    ) : Action

    /** 48 round bell, icon 24 `muted`, optional badge; [label] is the aria-label (AndHome:23). */
    data class Bell(
        val label: String,
        val badge: String? = null,
        override val onClick: (() -> Unit)? = null,
    ) : Action

    /** Text button, 14 / 500 `primary`, [height] 40 or 48. A non-null [testTag] identifies the button for UI tests. */
    data class Text(
        val label: String,
        val height: Int = 40,
        override val onClick: (() -> Unit)? = null,
        val testTag: String? = null,
    ) : Action
}

/** Horizontal padding and gap of the header row. */
enum class BarPadding(val start: Dp, val end: Dp, val gap: Dp) {
    Nav(4.dp, 4.dp, 4.dp),
    NavCounter(4.dp, 16.dp, 4.dp),
    TopLevelAction(16.dp, 4.dp, 0.dp),
    TitleOnly(16.dp, 16.dp, 0.dp),
    Modal(4.dp, 8.dp, 4.dp),
}

private fun Modifier.optionalClick(onClick: (() -> Unit)?): Modifier =
    if (onClick != null) clickable(onClick = onClick) else this

private fun Modifier.optionalTestTag(tag: String?): Modifier = if (tag != null) testTag(tag) else this

/** Top bar: status bar spacer 32 plus a 64 high header. */
@Composable
fun BaupilotTopAppBar(
    title: String,
    padding: BarPadding,
    navigation: Nav? = null,
    subtitle: String? = null,
    titleStyle: TitleStyle = TitleStyle.Large,
    actions: List<Action> = emptyList(),
    trailingText: String? = null,
    iconColor: ColorToken = Tokens.text,
    titleColor: ColorToken = Tokens.text,
    mutedColor: ColorToken = Tokens.muted,
    actionColor: ColorToken = Tokens.primary,
    badgeColor: ColorToken = Tokens.error,
    onBadgeColor: ColorToken = Tokens.onError,
) {
    baupilotTopAppBarMeasured(
        title = title,
        padding = padding,
        navigation = navigation,
        subtitle = subtitle,
        titleStyle = titleStyle,
        actions = actions,
        trailingText = trailingText,
        iconColor = iconColor,
        titleColor = titleColor,
        mutedColor = mutedColor,
        actionColor = actionColor,
        badgeColor = badgeColor,
        onBadgeColor = onBadgeColor,
    )
}

/** Same bar with a layout hook for the metric tests: [headerModifier] sits on the 64 high header. */
@Composable
internal fun baupilotTopAppBarMeasured(
    title: String,
    padding: BarPadding,
    headerModifier: Modifier = Modifier,
    navigation: Nav? = null,
    subtitle: String? = null,
    titleStyle: TitleStyle = TitleStyle.Large,
    actions: List<Action> = emptyList(),
    trailingText: String? = null,
    iconColor: ColorToken = Tokens.text,
    titleColor: ColorToken = Tokens.text,
    mutedColor: ColorToken = Tokens.muted,
    actionColor: ColorToken = Tokens.primary,
    badgeColor: ColorToken = Tokens.error,
    onBadgeColor: ColorToken = Tokens.onError,
) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(Tokens.Layout.statusBar))
        Row(
            modifier = headerModifier
                .fillMaxWidth()
                .height(Tokens.Layout.topAppBar)
                .padding(PaddingValues(start = padding.start, end = padding.end)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(padding.gap),
        ) {
            if (navigation != null) NavButton(navigation, iconColor)
            Column(Modifier.weight(1f).then(wholeDpCentered())) {
                BaupilotText(
                    text = title,
                    size = titleStyle.size,
                    lineHeight = titleStyle.lineHeight,
                    color = titleColor,
                    maxLines = if (titleStyle == TitleStyle.Compact) 1 else Int.MAX_VALUE,
                    overflow = if (titleStyle == TitleStyle.Compact) TextOverflow.Ellipsis else TextOverflow.Clip,
                )
                if (subtitle != null) {
                    BaupilotText(text = subtitle, size = 13.sp, color = mutedColor)
                }
            }
            if (trailingText != null) {
                BaupilotText(
                    text = trailingText, size = 14.sp, weight = 500, color = mutedColor,
                    modifier = wholeDpCentered(),
                )
            }
            actions.forEach { action ->
                when (action) {
                    is Action.Icon -> RoundButton(action.label, action.onClick) {
                        Icon(action.icon, 22.dp, mutedColor.color())
                    }

                    is Action.Bell -> RoundButton(action.label, action.onClick) {
                        Icon(BaupilotIcon.Bell, 24.dp, mutedColor.color())
                        if (action.badge != null) {
                            Badge(action.badge, badgeColor, onBadgeColor)
                        }
                    }

                    is Action.Text -> Box(
                        modifier = Modifier
                            .optionalTestTag(action.testTag)
                            .height(action.height.dp)
                            .clip(RoundedCornerShape((action.height / 2).dp))
                            .optionalClick(action.onClick)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BaupilotText(text = action.label, size = 14.sp, weight = 500, color = actionColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun NavButton(nav: Nav, iconColor: ColorToken) {
    RoundButton(nav.label, nav.onClick, Modifier.testTag(if (nav is Nav.Back) "nav.back" else "nav.close")) {
        Icon(
            icon = if (nav is Nav.Back) BaupilotIcon.ArrowLeft else BaupilotIcon.X,
            size = 24.dp,
            tint = iconColor.color(),
        )
    }
}

@Composable
private fun RoundButton(
    label: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .optionalClick(onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun BoxScope.Badge(
    text: String,
    background: ColorToken,
    foreground: ColorToken,
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 11.dp, end = 12.dp)
            .widthIn(min = 16.dp)
            .height(16.dp)
            .background(background.color(), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        BaupilotText(text = text, size = 11.sp, weight = 500, color = foreground)
    }
}
