package de.baupilot.snapshot

import de.baupilot.app.AppNavigationState
import de.baupilot.app.AppRoute
import de.baupilot.app.AppTab
import de.baupilot.app.NavigationTarget

/** Navigation state of every screen, by reference name, in `design/screens.md` order. */
object ScreenCatalog {
    val all: List<Pair<String, AppNavigationState>> = listOf(
        "Login" to AppNavigationState(),
        "Home" to AppNavigationState().logIn(),
        "Zeitplan" to AppNavigationState().logIn().select(AppTab.Schedule),
        "Profil" to AppNavigationState().logIn().select(AppTab.Profile),
        "Gewerk" to AppNavigationState().logIn().open(AppRoute.Trade("screed")),
        "Dokumente" to AppNavigationState().logIn().select(AppTab.Documents),
        "DokViewer" to AppNavigationState().logIn().select(AppTab.Documents).open(AppRoute.Document("doc-offer-wagner")),
        "Vergleich" to AppNavigationState().logIn().open(AppRoute.Trade("screed")).open(AppRoute.Compare("screed")),
        "Mitteilungen" to AppNavigationState().logIn().open(AppRoute.Notifications),
        "AbnGewerke" to AppNavigationState().logIn().select(AppTab.Inspections),
        "AbnElemente" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("windows")),
        "AbnPruefung" to AppNavigationState().logIn().select(AppTab.Inspections)
            .open(AppRoute.Inspection("windows"))
            .open(AppRoute.InspectionStep("windows", "F8", 3)),
        "AbnMangel" to AppNavigationState().logIn().select(AppTab.Inspections)
            .open(AppRoute.Inspection("windows"))
            .open(AppRoute.InspectionStep("windows", "F8", 3))
            .open(AppRoute.NewDefect),
        "AbnSignatur" to AppNavigationState().logIn().select(AppTab.Inspections)
            .open(AppRoute.Inspection("windows"))
            .open(AppRoute.SignOff("windows")),
        "AbnMaengel" to AppNavigationState().logIn().go(NavigationTarget.Defects),
        "Erd" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("earthworks")),
        "Rohbau" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("shell")),
        "RohbauMasse" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("shell"))
            .open(AppRoute.InspectionCheck("shell", "measure")),
        "Zimmerer" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("carpentry")),
        "ElUebersicht" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical")),
        "ElRaum" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
            .open(AppRoute.InspectionRoom("electrical", "kitchen")),
        "ElVerteiler" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
            .open(AppRoute.InspectionCheck("electrical", "panel")),
        "ElRauchmelder" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
            .open(AppRoute.InspectionCheck("electrical", "smoke")),
        "SanUebersicht" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing")),
        "SanGefaelle" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
            .open(AppRoute.InspectionCheckItem("plumbing", "slope", "A3")),
        "SanDruck" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
            .open(AppRoute.InspectionCheck("plumbing", "pressure")),
        "SanBad" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
            .open(AppRoute.InspectionRoom("plumbing", "bathUpper")),
        "EsUebersicht" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("screed")),
        "EsEbenheit" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("screed"))
            .open(AppRoute.InspectionCheck("screed", "flatness")),
        "EsBelegreife" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("screed"))
            .open(AppRoute.InspectionCheck("screed", "readiness")),
        "FlUebersicht" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("tiles")),
        "FlBad" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("tiles"))
            .open(AppRoute.InspectionRoom("tiles", "bath")),
        "MaUebersicht" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("painting")),
        "MaRaum" to AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("painting"))
            .open(AppRoute.InspectionRoom("painting", "living")),
    )

    fun state(name: String): AppNavigationState =
        all.firstOrNull { it.first == name }?.second ?: throw IllegalArgumentException("unknown screen $name")
}
