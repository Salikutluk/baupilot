package de.baupilot.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import de.baupilot.designsystem.components.PhaseCard
import de.baupilot.designsystem.components.PhaseCardState

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
}
