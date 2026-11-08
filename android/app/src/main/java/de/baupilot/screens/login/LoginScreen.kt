package de.baupilot.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.LoginModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.AssistChip
import de.baupilot.designsystem.components.BaupilotOutlinedTextField
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.FieldLook
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.GoogleMark
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.TextButton
import de.baupilot.designsystem.components.TextButtonSize
import de.baupilot.screens.ScreenCopy

/** Chips per row: 99 + 8 + 108 + 8 + 120 = 343 dp against 342 available, so the split is explicit (D19). */
private const val CHIPS_PER_ROW = 2

/**
 * Login (AndLogin.dc.html): padding 56 / 24 / 28, gap 24, the sign-in block pinned to the bottom.
 * E-Mail draws the focused look and Passwort the idle look, fixed (D15). "Anmelden" calls [onSubmit]; the Google
 * button, "Passwort vergessen?" and "Neues Bauvorhaben anlegen" are placeholders without action.
 */
@Composable
fun LoginScreen(model: LoginModel, onSubmit: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.bg.color())
            .padding(start = 24.dp, top = 56.dp, end = 24.dp, bottom = 28.dp)
            .testTag("screen.login"),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Tokens.primaryContainer.color(), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(BaupilotIcon.House, 28.dp, Tokens.onPrimaryContainer.color())
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BaupilotText(text = model.title, size = 32.sp, color = Tokens.text, lineHeight = 40.sp)
            BaupilotText(
                text = model.intro,
                size = 16.sp,
                color = Tokens.muted,
                lineHeight = 24.sp,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            model.chips.chunked(CHIPS_PER_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { AssistChip(it, leadingCheck = true) }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BaupilotOutlinedTextField(
                label = model.emailLabel,
                value = email,
                onValueChange = { email = it },
                placeholder = model.emailPlaceholder,
                look = FieldLook.Focused,
                modifier = Modifier.fillMaxWidth(),
            )
            BaupilotOutlinedTextField(
                label = model.passwordLabel,
                value = password,
                onValueChange = { password = it },
                placeholder = ScreenCopy.Login.PASSWORD_PLACEHOLDER,
                look = FieldLook.Idle,
                isPassword = true,
                modifier = Modifier.fillMaxWidth(),
            )
            // AndLogin.dc.html:42 (placeholder link, no action)
            TextButton(
                label = model.forgot,
                modifier = Modifier
                    .align(Alignment.End)
                    .margin(top = (-12).dp, end = (-12).dp, bottom = (-4).dp),
            )
        }
        Spacer(Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledButton(
                label = model.submit,
                size = ButtonSize.Regular,
                container = FilledContainer.Primary,
                letterSpacing = 0.1.sp,
                onClick = onSubmit,
                modifier = Modifier.testTag("login.submit"),
            )
            // AndLogin.dc.html:45 (placeholder, no action)
            OutlinedButton(
                label = model.social,
                contentColor = Tokens.text,
                leading = { GoogleMark(ScreenCopy.Login.SOCIAL_MARK) },
            )
            // AndLogin.dc.html:49 (placeholder link, no action)
            TextButton(label = model.create, size = TextButtonSize.Wide44)
        }
    }
}

/**
 * CSS margin, which may be negative: the layout box is the child's box grown by the margins, and the child is placed
 * at the start / top margin. Compose padding cannot be negative.
 */
private fun Modifier.margin(start: Dp = 0.dp, top: Dp = 0.dp, end: Dp = 0.dp, bottom: Dp = 0.dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    val width = (placeable.width + (start + end).roundToPx()).coerceAtLeast(0)
    val height = (placeable.height + (top + bottom).roundToPx()).coerceAtLeast(0)
    layout(width, height) { placeable.placeRelative(start.roundToPx(), top.roundToPx()) }
}
