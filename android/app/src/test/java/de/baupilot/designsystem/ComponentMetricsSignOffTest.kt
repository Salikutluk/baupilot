package de.baupilot.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.CaptionedActionBar
import de.baupilot.designsystem.components.CountTile
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.SignatureCard
import de.baupilot.designsystem.components.SignatureCardStyle
import de.baupilot.inspection.Extent
import de.baupilot.inspection.Point
import de.baupilot.inspection.SignatureStrokes
import de.baupilot.screens.signoff.SignatureStrokesSaver
import de.baupilot.snapshot.NoopSnapshotHandler
import de.baupilot.snapshot.baupilotPaparazzi
import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Geometry of the sign-off parts, measured from the layout (px = dp x 3) and the strokes saver. */
class ComponentMetricsSignOffTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(handler = NoopSnapshotHandler)

    private val sizes = mutableMapOf<String, Pair<Int, Int>>()

    private fun Modifier.record(key: String): Modifier = onGloballyPositioned {
        sizes[key] = it.size.width to it.size.height
    }

    @Test
    fun ownerCardIs104AndCountTileIs114x() {
        paparazzi.snapshot {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CountTile("12", "geprüft", Tokens.surfaceContainerHigh, Tokens.text, Modifier.weight(1f).record("tile"))
                    CountTile("11", "in Ordnung", Tokens.okBg, Tokens.okFg, Modifier.weight(1f))
                    CountTile("1", "Mangel", Tokens.errBg, Tokens.errFg, Modifier.weight(1f))
                }
                SignatureCard(
                    SignatureCardStyle.Pending("Bauherr", "Zum Unterschreiben tippen", SignatureStrokes(Extent(0f, 0f)), {}),
                    Modifier.record("owner"),
                )
                SignatureCard(SignatureCardStyle.Signed("Handwerker", "Name", "Unterschrieben", "Unterschrift"), Modifier.record("signed"))
                CaptionedActionBar("Hinweis", Modifier.record("bar")) {
                    FilledButton("Protokoll abschließen", ButtonSize.Regular, FilledContainer.Primary)
                }
            }
        }

        assertEquals(104 * 3, sizes.getValue("owner").second)
        // padding 12 + 24 / 500 value + gap 2 + 13 label + padding 12 on a 390 dp screen minus 2 x 16 gutters
        assertEquals(114 * 3, sizes.getValue("tile").first)
        // signed tile: 1 border + padding 12 around the text column 14 + 2 + 19 + 2 + 15 = 52 (taller than the 48 sample)
        assertEquals(78 * 3, sizes.getValue("signed").second)
        // padding 12 + button 48 + gap 6 + one 12 sp caption line + padding 24: the caption line is 12 to 16 dp
        val caption = sizes.getValue("bar").second - (12 + 48 + 6 + 24) * 3
        assertTrue("caption line $caption px", caption in 12 * 3..16 * 3)
    }

    @Test
    fun saverRoundTripsStrokes() {
        val strokes = SignatureStrokes(Extent(358f, 104f))
            .begin(Point(1f, 2f)).extend(Point(3f, 4f)).end()
            .begin(Point(5f, 6f)).end()
        val saved = with(SignatureStrokesSaver) { SaverScope { true }.save(strokes) }!!
        val restored = SignatureStrokesSaver.restore(saved)!!

        assertEquals(strokes.bounds, restored.bounds)
        assertEquals(listOf(listOf(Point(1f, 2f), Point(3f, 4f)), listOf(Point(5f, 6f))), restored.strokes)
        assertEquals(false, restored.isDrawing)
    }
}
