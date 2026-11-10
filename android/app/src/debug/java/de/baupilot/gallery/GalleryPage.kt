package de.baupilot.gallery

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.components.BaupilotVerticalStepper
import de.baupilot.designsystem.components.ObjectCheckRow
import de.baupilot.designsystem.components.ObjectChip
import de.baupilot.designsystem.components.ObjectChipState
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.PhaseCard
import de.baupilot.designsystem.components.PhaseCardState
import de.baupilot.designsystem.components.SlopeResult
import de.baupilot.designsystem.components.SocketTile
import de.baupilot.designsystem.components.SocketTileStyle
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
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
