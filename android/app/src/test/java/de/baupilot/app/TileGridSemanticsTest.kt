package de.baupilot.app

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.platform.testTag
import de.baupilot.designsystem.components.TileGrid
import de.baupilot.designsystem.components.TileItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D8: the FlBad grid is one node with the grid label and the hollow-tile labels as state; tiles have no role. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class TileGridSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    private fun tiles() = List(24) {
        val hollow = it == 2 || it == 3
        TileItem(
            id = "tile-${it + 1}",
            hollow = hollow,
            label = if (hollow) "hohl" else null,
            contentDescription = "Fliese ${it + 1}${if (hollow) ": hohl" else ""}",
            modifier = Modifier.testTag("tiles.tile.tile-${it + 1}"),
        )
    }

    @Test
    fun oneNodeWithDescriptionAndHollowTilesAsState() {
        rule.setContent { TileGrid(tiles(), 8, Modifier, accessibilityLabel = "Fliesenraster") }
        val config = rule.onNodeWithTag("tiles.grid").fetchSemanticsNode().config
        assertEquals(listOf("Fliesenraster"), config[SemanticsProperties.ContentDescription])
        assertEquals("Fliese 3: hohl, Fliese 4: hohl", config[SemanticsProperties.StateDescription])
        assertNull(config.getOrNull(SemanticsProperties.Role))
    }

    @Test
    fun tilesAreNoButtonsInTheMergedTree() {
        rule.setContent { TileGrid(tiles(), 8, Modifier, accessibilityLabel = "Fliesenraster") }
        assertEquals(0, rule.onAllNodesWithTag("tiles.tile.tile-3").fetchSemanticsNodes().size)
        val buttons = rule.onAllNodes(androidx.compose.ui.test.SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        assertEquals(0, buttons.fetchSemanticsNodes().size)
        // Unmerged: the tile exists as a tag carrier but has no button role of its own.
        val tile = rule.onNode(hasTestTag("tiles.tile.tile-3"), useUnmergedTree = true).fetchSemanticsNode()
        assertNull(tile.config.getOrNull(SemanticsProperties.Role))
    }

    @Test
    fun noHollowTileLeavesTheStateEmpty() {
        rule.setContent {
            TileGrid(
                tiles().map { TileItem(it.id, false, null, it.contentDescription, it.modifier) },
                8,
                Modifier,
                accessibilityLabel = "Fliesenraster",
            )
        }
        val config = rule.onNodeWithTag("tiles.grid").fetchSemanticsNode().config
        assertNull(config.getOrNull(SemanticsProperties.StateDescription))
    }
}
