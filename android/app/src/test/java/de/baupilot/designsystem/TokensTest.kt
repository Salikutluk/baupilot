package de.baupilot.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class TokensTest {
    @Test
    fun perScreenVariantDiffersFromCoreToken() {
        assertEquals(Color(0xFFE4D7CF), Tokens.track.abnElemente.light) // lint-hex: allow token value assertion
        assertEquals(Color(0xFFF0E0D6), Tokens.track.light) // lint-hex: allow token value assertion
    }

    @Test
    fun primaryLight() {
        assertEquals(Color(0xFF9C4300), Tokens.primary.light) // lint-hex: allow token value assertion
    }

    @Test
    fun supplementTokensExist() {
        assertEquals(Tokens.placeholder.light, Tokens.placeholder.dark)
        assertEquals(Tokens.fabShadow.light, Tokens.fabShadow.dark)
    }

    @Test
    fun documentPageTokensAreThemeIndependent() {
        val pairs = listOf(
            Tokens.docPaper, Tokens.docInk, Tokens.docBrand, Tokens.docMeta,
            Tokens.docPos, Tokens.docRule, Tokens.docRuleLight, Tokens.docShadow,
        )
        pairs.forEach { assertEquals(it.light, it.dark) }
        assertEquals(Color(0xFFFFFFFF), Tokens.docPaper.light) // lint-hex: allow token value assertion
        assertEquals(Color(0x2E000000), Tokens.docShadow.light) // lint-hex: allow token value assertion
    }

    @Test
    fun navPillLayout() {
        assertEquals(DpSize(56.dp, 32.dp), Tokens.Layout.navPill)
    }
}
