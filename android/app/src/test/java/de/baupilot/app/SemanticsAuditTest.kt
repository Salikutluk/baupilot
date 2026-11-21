package de.baupilot.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** One planted composable per rule of [SemanticsAudit], plus a clean control. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class SemanticsAuditTest {
    @get:Rule
    val rule = createComposeRule()

    private fun audit(content: @androidx.compose.runtime.Composable () -> Unit): List<Finding> {
        rule.setContent(content)
        rule.waitForIdle()
        return SemanticsAudit.check(rule.onRoot().fetchSemanticsNode())
    }

    @Test
    fun cleanControlHasNoFindings() {
        val findings = audit {
            Column {
                Box(Modifier.size(48.dp).testTag("ok").clickable { }) { Text("Speichern") }
                Image(ColorPainter(Color.Red), contentDescription = "Logo", modifier = Modifier.size(10.dp)) // lint-hex: allow solid test colour, not a screen colour
            }
        }
        assertEquals(emptyList<Finding>(), findings)
    }

    @Test
    fun clickableWithoutLabelIsReported() {
        val findings = audit { Box(Modifier.size(48.dp).testTag("bare").background(Color.Red).clickable { }) } // lint-hex: allow solid test colour, not a screen colour
        val hit = findings.single { it.rule == SemanticsAudit.NO_LABEL }
        assertEquals("bare", hit.testTag)
        assertEquals("0,0,48,48", hit.boundsDp)
    }

    @Test
    fun clickableWithContentDescriptionOnlyIsLabelled() {
        val findings = audit { Box(Modifier.size(48.dp).semantics { contentDescription = "Schließen" }.clickable { }) }
        assertTrue(findings.none { it.rule == SemanticsAudit.NO_LABEL })
    }

    @Test
    fun imageWithoutDescriptionIsReported() {
        val findings = audit { Image(ColorPainter(Color.Red), contentDescription = null, modifier = Modifier.size(10.dp).testTag("img").semantics { role = Role.Image }) } // lint-hex: allow solid test colour, not a screen colour
        assertEquals("img", findings.single { it.rule == SemanticsAudit.IMAGE_WITHOUT_DESCRIPTION }.testTag)
    }

    @Test
    fun smallClickableIsReported() {
        // Compose widens the touch bounds of every pointer-input node to the platform minimum (48 dp); the planted
        // composable turns that off, so the rule sees the real 30 dp.
        val findings = audit {
            CompositionLocalProvider(LocalViewConfiguration provides NoMinimumTouchTarget(LocalViewConfiguration.current)) {
                Box(Modifier.size(30.dp).testTag("small").clickable { }) { Text("Ok") }
            }
        }
        val hit = findings.single { it.rule == SemanticsAudit.TOUCH_TARGET }
        assertEquals("small", hit.testTag)
        assertEquals("0,0,30,30", hit.boundsDp)
    }

    @Test
    fun clickablesWithIdenticalBoundsAreReported() {
        val findings = audit {
            Box(Modifier.size(48.dp)) {
                Box(Modifier.size(48.dp).testTag("outer").clickable { }.semantics { contentDescription = "A" })
                Box(Modifier.size(48.dp).testTag("inner").clickable { }.semantics { contentDescription = "B" })
            }
        }
        assertEquals(setOf("outer", "inner"), findings.filter { it.rule == SemanticsAudit.DUPLICATE_BOUNDS }.map { it.testTag }.toSet())
    }
}

private class NoMinimumTouchTarget(base: ViewConfiguration) : ViewConfiguration by base {
    override val minimumTouchTargetSize: DpSize = DpSize.Zero
}
