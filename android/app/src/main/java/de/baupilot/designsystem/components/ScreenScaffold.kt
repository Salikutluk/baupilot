package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.Tokens

/**
 * Tab-root screen frame: `bg` over the whole screen, [topBar], then the scrolling [content] with padding
 * 8 16 0 and [spacing] between children. The optional [fab] sits at end 16 / bottom 96. The shell draws the
 * navigation bar over the bottom, so the scaffold has none.
 */
@Composable
fun TabScreenScaffold(
    topBar: @Composable () -> Unit,
    spacing: Dp,
    modifier: Modifier = Modifier,
    fab: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(Tokens.bg.color())) {
        Column(Modifier.fillMaxSize()) {
            topBar()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp)),
                verticalArrangement = Arrangement.spacedBy(spacing),
                content = content,
            )
        }
        if (fab != null) {
            Box(Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 96.dp)) { fab() }
        }
    }
}
