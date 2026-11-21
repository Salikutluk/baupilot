package de.baupilot.app

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import de.baupilot.screens.latertrades.EsBelegreifeScreen
import de.baupilot.snapshot.TestAppData
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** G-10b: the Aufheizprotokoll chart on EsBelegreife is one image node carrying the fixture's accessibility label. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class HeatingBarChartSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun chartIsOneImageNodeWithTheFixtureLabel() {
        val model = TestAppData.load().readinessChecks.values.single()
        rule.setContent { EsBelegreifeScreen(model = model, onBack = {}, modifier = Modifier) }

        val chart = rule.onNodeWithTag("readiness.chart")
        val config = chart.fetchSemanticsNode().config
        assertEquals(Role.Image, config[SemanticsProperties.Role])
        assertEquals(listOf(model.chart.accessibilityLabel), config[SemanticsProperties.ContentDescription])
        chart.onChildren().assertCountEquals(0)

        val images = rule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image)).fetchSemanticsNodes()
        assertEquals(1, images.size)
    }
}
