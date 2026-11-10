package de.baupilot.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.components.AlertBanner
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.NumberedList
import de.baupilot.designsystem.components.SocketGrid
import de.baupilot.designsystem.components.SocketItem
import de.baupilot.designsystem.components.SocketTile
import de.baupilot.designsystem.components.SocketTileStyle
import de.baupilot.designsystem.components.StatusList
import de.baupilot.designsystem.components.StatusRow
import de.baupilot.designsystem.components.checkHintRowMeasured
import de.baupilot.designsystem.components.PhaseCard
import de.baupilot.designsystem.components.PhaseCardState
import de.baupilot.designsystem.components.ProtocolDocumentButton
import de.baupilot.designsystem.components.ProtocolDocumentShape
import de.baupilot.designsystem.components.StatusChip
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize
import de.baupilot.designsystem.components.ValueStepper
import de.baupilot.snapshot.NoopSnapshotHandler
import de.baupilot.snapshot.baupilotPaparazzi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Geometry of the protocol parts, measured from the layout (px = dp x 3). */
class ComponentMetricsEarlyTradesTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(handler = NoopSnapshotHandler)

    private val heights = mutableMapOf<String, Int>()
    private val widths = mutableMapOf<String, Int>()

    private fun Modifier.record(key: String): Modifier = onGloballyPositioned {
        heights[key] = it.size.height
        widths[key] = it.size.width
    }

    /** AndErd:33: `height: 36px` plus a 1 px border outside = 38. */
    @Test
    fun protocolBannerDocumentButtonIs38Tall() {
        paparazzi.snapshot {
            Column {
                ProtocolDocumentButton("PDF", Tokens.okFg, onClick = null, modifier = Modifier.record("document"))
            }
        }
        assertEquals(38 * 3, heights.getValue("document"))
    }

    /** r18 exactly: 54 px at density 3, not clamped to a semicircle of the 38 dp box (r19). */
    @Test
    fun protocolBannerDocumentButtonRadiusIs18() {
        val radius = ProtocolDocumentShape.topStart.toPx(Size(300f, 114f), Density(3f))
        assertEquals(54f, radius, 0f)
    }

    /** The defaults reproduce the fixed 16 / 14 rows (I8). */
    @Test
    fun listItemTextSizesDefaultToToday() {
        paparazzi.snapshot {
            Column {
                BaupilotListItem("Titel", subtitle = "Untertitel", modifier = Modifier.record("default"))
                BaupilotListItem(
                    "Titel", subtitle = "Untertitel", titleSize = 16.sp, subtitleSize = 14.sp,
                    modifier = Modifier.record("explicit"),
                )
            }
        }
        assertEquals(heights.getValue("explicit"), heights.getValue("default"))
    }

    /** AndRohbauMasse:38-42: 48 inner + 1 `outline` border = 50 tall; 1 + 4 + 40 + 4 + 52 + 4 + 40 + 4 + 1 = 150 wide. */
    @Test
    fun valueStepperIs50By150() {
        paparazzi.snapshot {
            Column {
                ValueStepper("Breite", "1005", onDecrement = null, onIncrement = null, modifier = Modifier.record("stepper"))
            }
        }
        assertEquals(50 * 3, heights.getValue("stepper"))
        assertEquals(150 * 3, widths.getValue("stepper"))
    }

    /** `H32Body`: 32 tall, no width snapping, so the F1-F7 chips keep their natural fractional width (R12). */
    @Test
    fun bodyChipIs32TallWithoutWidthSnapping() {
        paparazzi.snapshot {
            Column {
                StatusChip("F1", StatusChipColors.Ok(), StatusChipSize.H32Body, Modifier.record("body"))
            }
        }
        assertEquals(32 * 3, heights.getValue("body"))
        // padding 12 + text + 12: the natural width is not forced to a whole dp
        assertTrue(widths.getValue("body") > 24 * 3)
    }

    /** Without icon and subtitle the banner is padding 12 + one title line + 12; the M4 call keeps its layout. */
    @Test
    fun alertBannerSlotsAreOptional() {
        paparazzi.snapshot {
            Column {
                AlertBanner(null, "Innerhalb der Toleranz", null, Modifier.record("textOnly"))
                AlertBanner(
                    BaupilotIcon.AlertTriangleW22, "Titel", "Untertitel", Modifier.record("full"),
                )
            }
        }
        assertTrue(heights.getValue("textOnly") < heights.getValue("full"))
    }

    /** AndElUebersicht:30: a grid row, so the shorter done card stretches to the active one; columns (390 - 32 - 8) / 2 = 175. */
    @Test
    fun phaseCardPairHasEqualHeightsWith175Columns() {
        paparazzi.snapshot {
            Row(
                Modifier.padding(horizontal = 16.dp).height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PhaseCard("1 · Rohinstallation", "Abgenommen 29.01.", modifier = Modifier.weight(1f).fillMaxHeight().record("done"))
                PhaseCard(
                    "2 · Fertiginstallation", "4 von 11 geprüft", PhaseCardState.Active(0.36f, Tokens.track.elUebersicht),
                    Modifier.weight(1f).fillMaxHeight().record("active"),
                )
            }
        }
        assertEquals(heights.getValue("active"), heights.getValue("done"))
        assertEquals(175 * 3, widths.getValue("done"))
        assertEquals(175 * 3, widths.getValue("active"))
    }

    /** done = 1 + 12 + label + 4 + value + 12 + 1 = 30 + text; active = 12 + label + 6 + value + 6 + 4 + 12 = 40 + text. */
    @Test
    fun activePhaseCardAddsTheBarAndTheWiderGaps() {
        paparazzi.snapshot {
            Column {
                PhaseCard("1 · Rohinstallation", "Abgenommen 29.01.", modifier = Modifier.record("done"))
                PhaseCard(
                    "2 · Fertiginstallation", "4 von 11 geprüft", PhaseCardState.Active(0.36f, Tokens.track.elUebersicht),
                    Modifier.record("active"),
                )
            }
        }
        // done = 1 + 12 + label + 4 + value + 12 + 1; active = 12 + label + 6 + value + 6 + 4 + 12
        assertEquals(heights.getValue("done") + 10 * 3, heights.getValue("active"))
    }

    private fun socket(id: String, border: ColorToken? = null) =
        SocketItem(id, "korrekt", SocketTileStyle(Tokens.okBg, Tokens.okFg, border), "Steckdose $id: korrekt")

    /** AndElRaum:49: `height: 52px`; the 2 dp `now` border is drawn inside, so it does not grow the tile. */
    @Test
    fun socketTileIs52TallWithAndWithoutBorder() {
        paparazzi.snapshot {
            Column {
                val plain = socket("S1")
                val now = socket("S6", Tokens.primary)
                SocketTile(plain.id, plain.label, plain.style, plain.contentDescription, Modifier.record("plain"))
                SocketTile(now.id, now.label, now.style, now.contentDescription, Modifier.record("now"))
            }
        }
        assertEquals(52 * 3, heights.getValue("plain"))
        assertEquals(52 * 3, heights.getValue("now"))
    }

    /** 4 columns, gap 8: a partly filled second row keeps its height; the grid also lives under `IntrinsicSize.Min`. */
    @Test
    fun socketGridRowsAre52HighWithGap8AndSurviveIntrinsicMeasurement() {
        paparazzi.snapshot {
            Column {
                Row(Modifier.height(IntrinsicSize.Min)) {
                    SocketGrid((1..8).map { socket("S$it") }, modifier = Modifier.weight(1f).record("eight"))
                }
                SocketGrid((1..4).map { socket("S$it") }, modifier = Modifier.record("four"))
                SocketGrid((1..5).map { socket("S$it") }, modifier = Modifier.record("five"))
            }
        }
        assertEquals((52 + 8 + 52) * 3, heights.getValue("eight"))
        assertEquals(52 * 3, heights.getValue("four"))
        assertEquals((52 + 8 + 52) * 3, heights.getValue("five"))
    }

    /** AndElVerteiler:45: 14 / 20 lines, gap 4. */
    @Test
    fun numberedListStacksItemsWithGap4() {
        paparazzi.snapshot {
            Column {
                NumberedList(listOf("Eins", "Zwei", "Drei"), color = Tokens.muted, modifier = Modifier.record("list"))
            }
        }
        assertEquals((3 * 20 + 2 * 4) * 3, heights.getValue("list"))
    }

    /** AndElVerteiler:53: `min-height: 56px`; the 1 dp divider sits between the rows. */
    @Test
    fun statusListRowMinHeightIs56WithDividerBetweenRows() {
        fun row(title: String) = StatusRow(title, "Bad", "Ausgelöst", StatusChipColors.Ok())
        paparazzi.snapshot {
            Column {
                StatusList(listOf(row("FI 1")), Tokens.bg, Tokens.outlineVariant, Modifier.record("one"))
                StatusList(listOf(row("FI 1"), row("FI 2")), Tokens.bg, Tokens.outlineVariant, Modifier.record("two"))
            }
        }
        assertEquals(56 * 3, heights.getValue("one"))
        assertEquals((56 + 1 + 56) * 3, heights.getValue("two"))
    }

    /** AndElRauchmelder:44: the check box is a 22 dp square, done or open. */
    @Test
    fun checkHintBoxIs22Square() {
        paparazzi.snapshot {
            Column {
                checkHintRowMeasured(
                    "Prüftaste", "Taste drücken", done = true, border = Tokens.onPrimaryContainer,
                    fill = Tokens.onPrimaryContainer, tick = Tokens.primaryContainer,
                    boxModifier = Modifier.record("done"),
                )
                checkHintRowMeasured(
                    "Prüftaste", "Taste drücken", done = false, border = Tokens.onPrimaryContainer,
                    fill = Tokens.onPrimaryContainer, tick = Tokens.primaryContainer,
                    boxModifier = Modifier.record("open"),
                )
            }
        }
        assertEquals(22 * 3, heights.getValue("done"))
        assertEquals(22 * 3, widths.getValue("done"))
        assertEquals(22 * 3, heights.getValue("open"))
        assertEquals(22 * 3, widths.getValue("open"))
    }

    /** Defaults reproduce the earlier rendering: the explicit colours equal the defaults (I8). */
    @Test
    fun infoCalloutDefaultsKeepTheEarlierColours() {
        paparazzi.snapshot {
            Column {
                InfoCallout("Text", modifier = Modifier.record("default"))
                InfoCallout(
                    "Text", container = Tokens.warnBg, content = Tokens.warnFg, icon = BaupilotIcon.BoltW22,
                    modifier = Modifier.record("warn"),
                )
            }
        }
        assertEquals(heights.getValue("default"), heights.getValue("warn"))
    }

    /** AndElRauchmelder:57: the dot is 10 dp, 7 dp from the start, in a 48 dp row. */
    @Test
    fun dotLeadingKeepsTheRowAt48() {
        paparazzi.snapshot {
            Column {
                BaupilotListItem(
                    "Flur OG", leading = Leading.Dot(Tokens.ok), minHeight = ListItemHeight.H48, horizontalPadding = 0.dp,
                    modifier = Modifier.record("row"),
                )
            }
        }
        assertEquals(48 * 3, heights.getValue("row"))
    }
}

