package de.baupilot.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens

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
