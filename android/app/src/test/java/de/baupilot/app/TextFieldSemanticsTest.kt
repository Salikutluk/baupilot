package de.baupilot.app

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import de.baupilot.designsystem.components.BaupilotOutlinedTextField
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** D-R7: a labelled field is one TalkBack stop for its label, not two (semantic description plus the visible notch text). */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class TextFieldSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun labelIsOneStop() {
        rule.setContent {
            BaupilotOutlinedTextField(label = "E-Mail", value = "", onValueChange = {}, placeholder = "name@beispiel.de", modifier = Modifier)
        }
        val carriesLabel = SemanticsMatcher("ContentDescription or Text equals the label") { node ->
            val config = node.config
            val description = config.getOrNull(SemanticsProperties.ContentDescription)
            val text = config.getOrNull(SemanticsProperties.Text)
            description?.contains("E-Mail") == true || text?.any { it.text == "E-Mail" } == true
        }
        assertEquals(1, rule.onAllNodes(carriesLabel).fetchSemanticsNodes().size)
    }
}
