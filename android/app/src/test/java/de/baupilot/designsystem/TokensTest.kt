package de.baupilot.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class TokensTest {
    @Test
    fun perScreenVariantDiffersFromCoreToken() {
        assertEquals(Color(0xFFE4D7CF), Tokens.track.abnElemente.light)
        assertEquals(Color(0xFFF0E0D6), Tokens.track.light)
    }

    @Test
    fun primaryLight() {
        assertEquals(Color(0xFF9C4300), Tokens.primary.light)
    }

    @Test
    fun navPillLayout() {
        assertEquals(DpSize(56.dp, 32.dp), Tokens.Layout.navPill)
    }
}
