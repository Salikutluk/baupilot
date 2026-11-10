package de.baupilot.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.app.HeatingBar
import de.baupilot.app.HeatingBarKind
import de.baupilot.designsystem.components.BaupilotVerticalStepper
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.CheckMetaRow
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.FactGrid
import de.baupilot.designsystem.components.FactItem
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.HeatingBarChart
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.ObjectCheckRow
import de.baupilot.designsystem.components.ObjectChip
import de.baupilot.designsystem.components.ObjectChipState
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.PhaseCard
import de.baupilot.designsystem.components.PhaseCardState
import de.baupilot.designsystem.components.RakingLightSketch
import de.baupilot.designsystem.components.SlopeResult
import de.baupilot.designsystem.components.SocketTile
import de.baupilot.designsystem.components.SocketTileStyle
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import de.baupilot.designsystem.components.TextLink
import de.baupilot.designsystem.components.LegendRow
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.Trailing
import de.baupilot.designsystem.components.TileGrid
import de.baupilot.designsystem.components.TileItem
import de.baupilot.designsystem.components.TimerRing
import de.baupilot.designsystem.components.ValueStepper

/** One full-screen gallery page; [goldenName] is the snapshot name. */
enum class GalleryPage(val goldenName: String) {
    Bars("GalleryBars"),
    BarsMore("GalleryBarsMore"),
    Lists("GalleryLists"),
    Cards("GalleryCards"),
    Chips("GalleryChips"),
    Buttons("GalleryButtons"),
    Inputs("GalleryInputs"),
    Icons("GalleryIcons"),
    Forms("GalleryForms"),
    Overview("GalleryOverview"),
    Schedule("GallerySchedule"),
    Settings("GallerySettings"),
    Trade("GalleryTrade"),
    Documents("GalleryDocuments"),
    Compare("GalleryCompare"),
    Notifications("GalleryNotifications"),
    Inspections("GalleryInspections"),
    InspectionElements("GalleryInspectionElements"),
    InspectionStep("GalleryInspectionStep"),
    DefectForm("GalleryDefectForm"),
    SignOff("GallerySignOff"),
    Protocol("GalleryProtocol"),
    Measure("GalleryMeasure"),
    Electrical("GalleryElectrical"),
    ElectricalSteps("GalleryElectricalSteps"),
    ElectricalSteps2("GalleryElectricalSteps2"),
    Plumbing("GalleryPlumbing"),
    PlumbingSteps("GalleryPlumbingSteps"),
    Screed("GalleryScreed"),
    Finishing("GalleryFinishing"),
}

@Composable
fun GalleryPageContent(page: GalleryPage) {
    GalleryPageFrame(page) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (page) {
                GalleryPage.Bars -> BarsPage()
                GalleryPage.BarsMore -> BarsMorePage()
                GalleryPage.Lists -> ListsPage()
                GalleryPage.Cards -> CardsPage()
                GalleryPage.Chips -> ChipsPage()
                GalleryPage.Buttons -> ButtonsPage()
                GalleryPage.Inputs -> InputsPage()
                GalleryPage.Icons -> IconsPage()
                GalleryPage.Forms -> FormsPage()
                GalleryPage.Overview -> OverviewPage()
                GalleryPage.Schedule -> SchedulePage()
                GalleryPage.Settings -> SettingsPage()
                GalleryPage.Trade -> TradePage()
                GalleryPage.Documents -> DocumentsPage()
                GalleryPage.Compare -> ComparePage()
                GalleryPage.Notifications -> NotificationsPage()
                GalleryPage.Inspections -> InspectionsPage()
                GalleryPage.InspectionElements -> InspectionElementsPage()
                GalleryPage.InspectionStep -> InspectionStepPage()
                GalleryPage.DefectForm -> DefectFormPage()
                GalleryPage.SignOff -> SignOffPage()
                GalleryPage.Protocol -> ProtocolPage()
                GalleryPage.Measure -> MeasurePage()
                GalleryPage.Electrical -> ElectricalPage()
                GalleryPage.ElectricalSteps -> ElectricalStepsPage()
                GalleryPage.ElectricalSteps2 -> ElectricalSteps2Page()
                GalleryPage.Plumbing -> PlumbingPage()
                GalleryPage.PlumbingSteps -> PlumbingStepsPage()
                GalleryPage.Screed -> ScreedPage()
                GalleryPage.Finishing -> FinishingPage()
            }
        }
    }
}

/**
 * Page background plus the page-height guard: the content is measured with the incoming width and
 * unbounded height and must fit the 844 dp screen.
 */
@Composable
private fun GalleryPageFrame(page: GalleryPage, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Tokens.bg.color())) {
        Layout(content = content) { measurables, constraints ->
            val placeable = measurables.single().measure(
                constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity),
            )
            checkPageHeight(page.name, placeable.height, 844.dp.roundToPx())
            layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
        }
    }
}

/** Fails when a page's content is taller than the screen it is recorded on. */
internal fun checkPageHeight(name: String, heightPx: Int, limitPx: Int) {
    check(heightPx <= limitPx) { "Galerie-Seite $name zu hoch: $heightPx > $limitPx" }
}

/** Developer label above a sample (11 / 500 `muted`). */
@Composable
internal fun Caption(text: String) {
    BaupilotText(text = text, size = 11.sp, weight = 500, color = Tokens.muted)
}

/** Sanitär parts, first page: the planned PhaseCard next to the active one, same height, 2 × 175 (AndSanUebersicht:30). */
@Composable
internal fun PlumbingPage() {
    Caption("PhaseCard · Active und Planned, gleiche Höhe, 2 × 175 (AndSanUebersicht:30)")
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PhaseCard(
            "1 · Rohinstallation", "3 von 6 geprüft",
            PhaseCardState.Active(0.5f, Tokens.track.sanUebersicht),
            Modifier.weight(1f).fillMaxHeight(),
        )
        PhaseCard(
            "2 · Fertigmontage", "Geplant KW 14", PhaseCardState.Planned,
            Modifier.weight(1f).fillMaxHeight(),
        )
    }
    Caption("SocketTile · 52, ok / Mangel / jetzt / offen (AndSanGefaelle:21)")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SocketTile("A1", "1,4 %", SocketTileStyle(Tokens.okBg, Tokens.okFg), "A1 1,4 %", Modifier.weight(1f))
        SocketTile("A2", "0,4 %", SocketTileStyle(Tokens.errBg, Tokens.errFg), "A2 0,4 %", Modifier.weight(1f))
        SocketTile(
            "A3", "jetzt", SocketTileStyle(Tokens.primaryContainer, Tokens.onPrimaryContainer, Tokens.primary),
            "A3 jetzt", Modifier.weight(1f),
        )
        SocketTile("A4", "offen", SocketTileStyle(Tokens.openBg, Tokens.muted), "A4 offen", Modifier.weight(1f))
    }
    Caption("ValueStepper · fillsWidth, 2 × 158, gap 10 (AndSanGefaelle:44)")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ValueStepper("Länge", "2,0 m", null, null, Modifier.weight(1f), fillsWidth = true)
        ValueStepper("Höhenunterschied", "2,5 cm", null, null, Modifier.weight(1f), fillsWidth = true)
    }
    Caption("SlopeResult · ausreichend (1,25 %)")
    SlopeResult(
        percent = "1,25 %", verdict = "Ausreichend · Soll ≥ 1,0 %", sufficient = true, schemaY2 = 24.5,
        levelLabel = "waagerecht", deltaLabel = "Δh",
    )
    Caption("SlopeResult · zu flach (0,40 %)")
    SlopeResult(
        percent = "0,40 %", verdict = "Zu flach · Soll ≥ 1,0 %", sufficient = false, schemaY2 = 16.0,
        levelLabel = "waagerecht", deltaLabel = "Δh",
    )
}

/** Sanitär parts, second page: the timer ring, the Compact stepper with meta 500 / spacing 14, the 20 icon button, the check row and the object chips. */
@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun PlumbingStepsPage() {
    Caption("TimerRing · 72, Loch 58, 58 % (AndSanDruck:50)")
    TimerRing(0.58f, "12:40", "Prüfzeit zu 58 Prozent abgelaufen")
    Caption("Stepper Compact · meta 500 text, currentSpacing 14, openMinHeight 40 (AndSanDruck:36)")
    BaupilotVerticalStepper(
        steps = listOf(
            Step(1, "Leitungen sichtbar & beschriftet", StepState.Done, "3 Fotos", metaColor = Tokens.text, metaWeight = 500),
            Step(
                3, "Manometer am Ende ablesen", StepState.Current,
                body = { BaupilotText("Aktueller Schritt", size = 15.sp, color = Tokens.muted) },
            ),
            Step(5, "Druckprüfprotokoll unterschrieben", StepState.Open, tag = "Pflicht"),
        ),
        variant = StepperVariant.Compact,
        currentSpacing = 14.dp,
        openMinHeight = 40.dp,
    )
    Caption("OutlinedButton · Icon 20 (AndSanDruck:71)")
    OutlinedButton(label = "Manometer fotografieren", icon = BaupilotIcon.Camera, iconSize = 20.dp)
    Caption("ObjectCheckRow · in Ordnung, offen mit Trenner (AndSanBad:46)")
    Column {
        ObjectCheckRow("Warm links, kalt rechts", "Hebel ganz nach links: Wasser wird warm.", ok = true, showsDivider = false)
        ObjectCheckRow("Siphon & Anschlüsse trocken", "Mit Papiertuch unten abwischen.", ok = false, showsDivider = true)
    }
    Caption("ObjectChip · erledigt, gewählt, offen im FlowRow (AndSanBad:30)")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ObjectChip("WC", ObjectChipState.Done)
        ObjectChip("Waschtisch", ObjectChipState.Now)
        ObjectChip("Dusche", ObjectChipState.Open)
        ObjectChip("Heizkörper", ObjectChipState.Open)
        ObjectChip("Lüfter", ObjectChipState.Open)
    }
}

/** Estrich parts, page: the text link (AndEsEbenheit:56), the heating chart, the check rows, the compact button, the lock note. */
@Composable
internal fun ScreedPage() {
    Caption("TextLink · 14 / 500 err, Layout 44, Treffer 48 (AndEsEbenheit:56)")
    TextLink("1 Messpunkt über Grenzwert → Mangel erfassen", Tokens.err)
    Caption("TextLink · Platzhalter ohne Aktion, muted")
    TextLink("Kein Messpunkt über Grenzwert", Tokens.muted)
    Caption("HeatingBarChart · 80 hoch, Balken Lücke 3, ok / primary / track (AndEsBelegreife:30)")
    val plan = listOf(25, 25, 25, 30, 35, 35, 40, 45, 45, 45, 45, 45, 45, 40, 35, 30, 25, 25, 25, 25, 25)
    HeatingBarChart(
        bars = plan.mapIndexed { i, v ->
            HeatingBar(
                (2 * v * 72 + 45) / 90,
                if (i < 5) HeatingBarKind.Recorded else if (i == 5) HeatingBarKind.Today else HeatingBarKind.Planned,
            )
        },
        accessibilityLabel = "Vorlauftemperatur je Tag, Tag 1 bis 5 erfasst, heute Tag 6",
        recorded = Tokens.ok,
        today = Tokens.primary,
        planned = Tokens.track.esBelegreife,
    )
    Caption("CheckMetaRow · min 44, Kasten 20 r4 (AndEsBelegreife:55)")
    CheckMetaRow("Messstellen markiert", "wegen Fußbodenheizung", true)
    CheckMetaRow("Messung durchgeführt", "geplant 19.03.", false)
    Caption("FilledButton · Compact, Rand 20 (AndEsBelegreife:44)")
    FilledButton("Tag 6 eintragen", ButtonSize.Compact, FilledContainer.Primary, horizontalPadding = 20.dp)
    Caption("InfoCallout · surfaceContainer / muted, Lock, zentriert (AndEsBelegreife:65)")
    InfoCallout(
        "Bodenbelag erst nach Freigabe",
        icon = BaupilotIcon.Lock,
        container = Tokens.surfaceContainer,
        content = Tokens.muted,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    )
}

/** Fliesen parts, page: the tile grid with its legend (AndFlBad:50-53) and the error button pair (AndFlBad:58-59). */
@Composable
internal fun FinishingPage() {
    Caption("LegendRow · 12 / 600 muted, Swatch 12 errBg + 2 err, Abstand nach unten −6 (AndFlBad:50)")
    LegendRow("↑ Wand mit Dusche", "klingt hohl")
    Caption("TileGrid · 8 Spalten, r8, Innenabstand 8, Lücke 4, Fliese 44 r2, hohl errBg + 2 err (AndFlBad:51)")
    TileGrid(
        tiles = List(24) { index ->
            val hollow = index == 2 || index == 3
            TileItem("tile-${index + 1}", hollow, if (hollow) "hohl" else null, "Fliese ${index + 1}")
        },
        columns = 8,
        accessibilityLabel = "Fliesenraster Boden, oben die Wand mit Dusche, unten die Tür",
    )
    Caption("FilledButton Error + OutlinedButton primary · 152 / 166 (AndFlBad:58)")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledButton("Als Mangel", ButtonSize.Regular, FilledContainer.Error, Modifier.weight(152f))
        OutlinedButton("Keine Hohlstellen", Modifier.weight(166f))
    }
    Caption("ListItem Swatch · 40 rund, 1 outlineVariant, Status 13 / 500 (AndMaUebersicht:41-49)")
    BaupilotListItem(
        title = "Wohnen / Essen",
        subtitle = "Sand · Schritt 2 von 5",
        leading = Leading.Swatch(Tokens.paintSand),
        trailing = Trailing.Text("Läuft", 13.sp, Tokens.primary, 500),
    )
    Caption("FactGrid · surfaceContainer r12, Innenabstand 14 16, 3 Spalten, Lücke 8 (AndMaUebersicht:25)")
    FactGrid(listOf(FactItem("Qualität", "Q3"), FactItem("Farbtöne", "3 bemustert"), FactItem("Räume", "3 von 9")))
    Caption("RakingLightSketch · 326 × 88, r12, Verlauf 100° lit → wall 70 %, Lampe 34 × 18, Schatten bei 62 % (AndMaRaum:34-38)")
    RakingLightSketch(Modifier.width(326.dp))
    Caption("Step.trailing · Done-Zeile mit Farbfeld 22 außen, Compact (AndMaRaum:21-31)")
    BaupilotVerticalStepper(
        steps = listOf(
            Step(
                number = 1,
                title = "Farbton wie bemustert",
                state = StepState.Done,
                trailing = {
                    Box(Modifier.size(22.dp).background(Tokens.paintSand.color(), CircleShape).border(1.dp, Tokens.outlineVariant.color(), CircleShape))
                },
            ),
        ),
        variant = StepperVariant.Compact,
    )
}
