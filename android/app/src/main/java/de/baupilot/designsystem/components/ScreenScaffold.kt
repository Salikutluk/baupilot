package de.baupilot.designsystem.components

import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.Tokens

/**
 * Tab-root screen frame: `bg` over the whole screen, [topBar], then the scrolling [content] with
 * [contentPadding] (default 16 8 16 0: start, top, end, bottom) and [spacing] between children. The optional
 * [fab] sits at end 16 / bottom 96. The shell draws the navigation bar over the bottom, so the scaffold has none.
 */
@Composable
fun TabScreenScaffold(
    topBar: @Composable () -> Unit,
    spacing: Dp,
    modifier: Modifier = Modifier,
    fab: (@Composable () -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(Tokens.bg.color())) {
        Column(Modifier.fillMaxSize()) {
            topBar()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(spacing),
                content = content,
            )
        }
        if (fab != null) {
            Box(Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 96.dp)) { fab() }
        }
    }
}

private enum class DetailSlot { Body, Bar }

/**
 * Pushed detail screen frame (AndGewerk:13): `bg` over the whole screen, [topBar], then the scrolling [content]
 * with [contentPadding] and [spacing] between children. The optional [bottomBar] is pinned to the bottom edge
 * and drawn over the content, like the prototype's absolute bar. The bar is measured first and its height is
 * added below the scrolling content, so on a screen shorter than the content the last item can be scrolled
 * above the bar. [scrollState] lets a caller observe or drive the scroll position.
 */
@Composable
fun DetailScreenScaffold(
    topBar: @Composable () -> Unit,
    contentPadding: PaddingValues,
    spacing: Dp,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    SubcomposeLayout(modifier.fillMaxSize().background(Tokens.bg.color())) { constraints ->
        val bar = bottomBar
            ?.let { subcompose(DetailSlot.Bar, it) }
            .orEmpty()
            .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val barHeight = bar.maxOfOrNull { it.height } ?: 0
        val body = subcompose(DetailSlot.Body) {
            Column(Modifier.fillMaxSize()) {
                topBar()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(contentPadding)
                        .padding(bottom = barHeight.toDp()),
                    verticalArrangement = Arrangement.spacedBy(spacing),
                    content = content,
                )
            }
        }.map { it.measure(constraints) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            body.forEach { it.place(0, 0) }
            bar.forEach { it.place(0, constraints.maxHeight - it.height) }
        }
    }
}
