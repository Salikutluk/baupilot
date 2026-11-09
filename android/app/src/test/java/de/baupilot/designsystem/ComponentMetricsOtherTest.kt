package de.baupilot.designsystem

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.components.AskRow
import de.baupilot.designsystem.components.AvatarSize
import de.baupilot.designsystem.components.BaupilotAvatar
import de.baupilot.designsystem.components.BaupilotOutlinedTextField
import de.baupilot.designsystem.components.ScheduleChart
import de.baupilot.schedule.MonthLabel
import de.baupilot.schedule.ScheduleLayout
import de.baupilot.schedule.ScheduleRowLayout
import de.baupilot.schedule.ScheduleState
import de.baupilot.designsystem.components.BaupilotSearchBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.DocumentPreview
import de.baupilot.designsystem.components.DocumentTile
import de.baupilot.designsystem.components.HintCard
import de.baupilot.designsystem.components.PreviewLine
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.SectionLink
import de.baupilot.designsystem.components.FieldLook
import de.baupilot.designsystem.components.BaupilotLinearProgress
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import de.baupilot.designsystem.components.baupilotSwitchMeasured
import de.baupilot.designsystem.components.baupilotVerticalStepperMeasured
import de.baupilot.snapshot.NoopSnapshotHandler
import de.baupilot.snapshot.baupilotPaparazzi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Geometry of search bar, switch, stepper and progress bar, measured from the layout (px = dp x 3). */
class ComponentMetricsOtherTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(handler = NoopSnapshotHandler)

    private val sizes = mutableMapOf<String, Pair<Int, Int>>()
    private val offsets = mutableMapOf<String, Int>()

    private fun Modifier.record(key: String): Modifier = onGloballyPositioned {
        sizes[key] = it.size.width to it.size.height
        offsets[key] = it.positionInRoot().x.toInt()
    }

    @Test
    fun searchBarIs56() {
        paparazzi.snapshot {
            Column {
                BaupilotSearchBar(
                    query = "",
                    placeholder = "Suchen",
                    trailingText = "26",
                    onQueryChange = {},
                    modifier = Modifier.record("search"),
                )
            }
        }

        assertEquals(56 * 3, sizes.getValue("search").second)
    }

    @Test
    fun outlinedTextFieldIs56() {
        paparazzi.snapshot {
            Column {
                BaupilotOutlinedTextField(
                    label = "E-Mail",
                    value = "",
                    onValueChange = {},
                    placeholder = "name@beispiel.de",
                    look = FieldLook.Focused,
                    modifier = Modifier.record("focused"),
                )
                BaupilotOutlinedTextField(
                    label = "Passwort",
                    value = "",
                    onValueChange = {},
                    placeholder = "••••••••",
                    isPassword = true,
                    modifier = Modifier.record("idle"),
                )
            }
        }

        assertEquals(56 * 3, sizes.getValue("focused").second)
        assertEquals(56 * 3, sizes.getValue("idle").second)
    }

    @Test
    fun documentTileIs56() {
        paparazzi.snapshot {
            Column {
                DocumentTile(BaupilotIcon.Document, "Angebote", "7", onClick = null, modifier = Modifier.width(171.dp).record("tile"))
                SectionHeader("Als Nächstes benötigt", SectionHeaderStyle.Overview(SectionLink("Alle")), Modifier.record("withLink"))
                SectionHeader("Dokumente", SectionHeaderStyle.Overview(), Modifier.record("noLink"))
                SectionHeader("Konto", SectionHeaderStyle.Settings, Modifier.record("settings"))
            }
        }

        assertEquals(56 * 3, sizes.getValue("tile").second)
        assertEquals(40 * 3, sizes.getValue("withLink").second)
        assertEquals(32 * 3, sizes.getValue("noLink").second)
        val settings = sizes.getValue("settings").second
        assertTrue("settings header $settings", settings > (12 + 4) * 3)
    }

    @Test
    fun documentPreviewIs250x320() {
        paparazzi.snapshot {
            Column {
                DocumentPreview(
                    issuerName = "Estrichbau Wagner GmbH",
                    issuerAddress = "Industriestraße 4 · 68519 Viernheim",
                    number = "Angebot Nr. 2026-118",
                    reference = "BV Einfamilienhaus Musterweg 12 · gültig bis 31.01.2027",
                    lines = listOf(
                        PreviewLine("1", "Randdämmstreifen 8 mm, liefern & setzen", "420,00"),
                        PreviewLine("2", "PE-Folie als Trennlage", "310,00"),
                        PreviewLine("3", "Zementestrich CT-C25-F4, 65 mm, 112 m²", "7.150,00"),
                        PreviewLine("4", "Bewegungsfugen anlegen", "355,00"),
                        PreviewLine("5", "Baustelleneinrichtung", "1.565,00"),
                    ),
                    totalLabel = "Gesamt brutto",
                    total = "9.800,00 €",
                    contentDescription = "Vorschau Seite 1 von 2",
                    modifier = Modifier.record("page"),
                )
            }
        }

        assertEquals(250 * 3 to 320 * 3, sizes.getValue("page"))
    }

    @Test
    fun hintCardPaddingAndIcon() {
        paparazzi.snapshot {
            Column {
                HintCard(
                    icon = BaupilotIcon.Clock,
                    title = "Rechnung prüfen & zahlen",
                    subtitle = "Fenster & Türen · fällig 22.12.",
                    amount = "18.400 €",
                    modifier = Modifier.record("card"),
                )
                Column(Modifier.record("texts"), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    BaupilotText("Rechnung prüfen & zahlen", size = 16.sp, weight = 500, color = Tokens.onPrimaryContainer)
                    BaupilotText("Fenster & Türen · fällig 22.12.", size = 13.sp, color = Tokens.onPrimaryContainer)
                }
            }
        }

        // padding 12 + 12 around the text column (taller than the 24 icon); the card fills the 390 dp width.
        assertEquals(sizes.getValue("texts").second + 24 * 3, sizes.getValue("card").second)
        assertEquals(390 * 3, sizes.getValue("card").first)
    }

    @Test
    fun compactSectionHeaderLinkHas48HitArea() {
        paparazzi.snapshot {
            Column {
                SectionHeader("Zuletzt hinzugefügt", SectionHeaderStyle.Compact(SectionLink("Nach Gewerk")), Modifier.record("withLink"))
                SectionHeader("Zuletzt hinzugefügt", SectionHeaderStyle.Compact(null), Modifier.record("noLink"))
            }
        }

        assertEquals(48 * 3, sizes.getValue("withLink").second)
        assertTrue("noLink header ${sizes.getValue("noLink").second}", sizes.getValue("noLink").second < 48 * 3)
    }

    @Test
    fun avatarS56() {
        paparazzi.snapshot {
            Column {
                BaupilotAvatar("MM", AvatarSize.S56, Tokens.primaryContainer, Tokens.onPrimaryContainer, Modifier.record("s56"))
                BaupilotAvatar("MM", AvatarSize.S40, Tokens.primaryContainer, Tokens.onPrimaryContainer, Modifier.record("s40"))
            }
        }

        assertEquals(56 * 3 to 56 * 3, sizes.getValue("s56"))
        assertEquals(40 * 3 to 40 * 3, sizes.getValue("s40"))
    }

    @Test
    fun askRowMinHeight52() {
        paparazzi.snapshot {
            Column {
                AskRow("Estrich", "Start KW 8", onClick = null, modifier = Modifier.width(326.dp).record("row"))
            }
        }

        assertEquals(52 * 3, sizes.getValue("row").second)
    }

    @Test
    fun scheduleChartHeight() {
        val rows = List(3) { ScheduleRowLayout("t$it", "Gewerk $it", ScheduleState.Planned, 126f, 20f, null) }
        val layout = ScheduleLayout(rows, 200f, 186f, listOf(MonthLabel(126f, "Mär 26")))
        paparazzi.snapshot {
            Column {
                ScheduleChart(layout, "Heute", "Zeitplan", Modifier.width(358.dp).record("chart"))
            }
        }

        val labelLine = RobotoMetrics.lineBox(11f)
        assertEquals((20 + 3 * 34 + 4 + labelLine) * 3, sizes.getValue("chart").second.toFloat(), 0.5f)
    }

    @Test
    fun switchIs52x32AndThumbPositions() {
        paparazzi.snapshot {
            Column {
                baupilotSwitchMeasured(
                    checked = true,
                    contentDescription = "an",
                    onCheckedChange = {},
                    modifier = Modifier.record("on"),
                    thumbModifier = Modifier.record("onThumb"),
                )
                baupilotSwitchMeasured(
                    checked = false,
                    contentDescription = "aus",
                    onCheckedChange = {},
                    modifier = Modifier.record("off"),
                    thumbModifier = Modifier.record("offThumb"),
                )
            }
        }

        assertEquals(52 * 3 to 32 * 3, sizes.getValue("on"))
        assertEquals(52 * 3 to 32 * 3, sizes.getValue("off"))
        assertEquals(24 * 3 to 24 * 3, sizes.getValue("onThumb"))
        assertEquals(16 * 3 to 16 * 3, sizes.getValue("offThumb"))
        assertEquals(22 * 3, offsets.getValue("onThumb") - offsets.getValue("on"))
        assertEquals(6 * 3, offsets.getValue("offThumb") - offsets.getValue("off"))
    }

    @Test
    fun stepperCircleIs24() {
        paparazzi.snapshot {
            Column {
                baupilotVerticalStepperMeasured(
                    steps = listOf(
                        Step(number = 1, title = "Fenster", state = StepState.Done, meta = "ok"),
                        Step(number = 2, title = "Tür", state = StepState.Open),
                    ),
                    variant = StepperVariant.Rail,
                    circleModifier = Modifier.record("circle"),
                )
            }
        }

        assertEquals(24 * 3 to 24 * 3, sizes.getValue("circle"))
    }

    @Test
    fun linearProgressIs4() {
        paparazzi.snapshot {
            Column {
                BaupilotLinearProgress(progress = 0.42f, modifier = Modifier.record("bar"))
            }
        }

        assertEquals(4 * 3, sizes.getValue("bar").second)
    }

    @Test
    fun textLineBoxHeight() {
        paparazzi.snapshot {
            Column {
                BaupilotText(text = "Hg", size = 14.sp, color = Tokens.onPrimaryContainer, modifier = Modifier.record("default"))
                BaupilotText(
                    text = "Hg",
                    size = 14.sp,
                    color = Tokens.onPrimaryContainer,
                    lineHeight = TextUnit.Unspecified,
                    modifier = Modifier.record("explicit"),
                )
                BaupilotText(
                    text = "Hg",
                    size = 14.sp,
                    color = Tokens.onPrimaryContainer,
                    lineHeight = 28.sp,
                    modifier = Modifier.record("concrete"),
                )
            }
        }

        assertEquals(16 * 3, sizes.getValue("default").second)
        assertEquals(16 * 3, sizes.getValue("explicit").second)
        assertEquals(28 * 3, sizes.getValue("concrete").second)
    }

    @Test
    fun textBaselineMatchesRoundedAscent() {
        val sizesSp = listOf(11, 12, 13, 14, 15, 16, 18, 20, 22, 25)
        val baselines = mutableMapOf<Int, Int>()
        val heights = mutableMapOf<Int, Int>()
        paparazzi.snapshot {
            Column {
                for (size in sizesSp) {
                    BaupilotText(
                        text = "Hg",
                        size = size.sp,
                        color = Tokens.onPrimaryContainer,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.onGloballyPositioned {
                            baselines[size] = it[FirstBaseline]
                            heights[size] = it.size.height
                        },
                    )
                }
            }
        }

        for (size in sizesSp) {
            val box = RobotoMetrics.lineBox(size.toFloat())
            val base = RobotoMetrics.baselineOffset(size.toFloat())
            assertEquals("height $size", box * 3, heights.getValue(size).toFloat(), 0.5f)
            val deltaDp = Math.abs(baselines.getValue(size) / 3f - base)
            assertTrue("baseline $size sp: ${baselines.getValue(size) / 3f} dp, want $base", deltaDp <= 0.5f)
        }
    }

    @Test
    fun detailScaffoldReservesTheBottomBar() {
        val scroll = ScrollState(0)
        paparazzi.snapshot {
            Box(Modifier.size(390.dp, 400.dp)) {
                DetailScreenScaffold(
                    topBar = {},
                    contentPadding = PaddingValues(0.dp),
                    spacing = 0.dp,
                    scrollState = scroll,
                    bottomBar = { Spacer(Modifier.fillMaxWidth().height(90.dp).record("bar")) },
                ) {
                    Spacer(Modifier.fillMaxWidth().height(1000.dp))
                }
            }
        }

        assertEquals(90 * 3, sizes.getValue("bar").second)
        assertEquals((1000 + 90 - 400) * 3, scroll.maxValue)
    }
}
