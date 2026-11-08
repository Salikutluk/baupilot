package de.baupilot.screens.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.TabScreenScaffold

/** How a placeholder is reached: as a tab root (shell draws the bar) or pushed (back control, no tab bar). */
sealed interface PlaceholderPresentation {
    data object TabRoot : PlaceholderPresentation
    data class Pushed(val onBack: () -> Unit) : PlaceholderPresentation
}

/** Content-less screen for routes whose content arrives in M3/M4; the title is an existing string (D8). */
@Composable
fun PlaceholderScreen(
    title: String,
    presentation: PlaceholderPresentation,
    modifier: Modifier = Modifier,
) {
    when (presentation) {
        PlaceholderPresentation.TabRoot -> TabScreenScaffold(
            topBar = { BaupilotTopAppBar(title = title, padding = BarPadding.TitleOnly) },
            spacing = 0.dp,
            modifier = modifier,
        ) {}

        is PlaceholderPresentation.Pushed -> Column(
            modifier = modifier.fillMaxSize().background(Tokens.bg.color()),
        ) {
            BaupilotTopAppBar(
                title = title,
                padding = BarPadding.Nav,
                navigation = Nav.Back(onClick = presentation.onBack),
            )
        }
    }
}
