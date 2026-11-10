package de.baupilot.snapshot

import de.baupilot.app.AppNavigationState
import de.baupilot.app.AppRoute
import de.baupilot.app.AppTab
import de.baupilot.app.BaupilotAppContent
import de.baupilot.app.NavigationTarget
import org.junit.Rule
import org.junit.Test

/** Login while logged out, compared with `design/reference/android/Login_light.png`. */
class LoginLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Login", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun login() {
        val data = TestAppData.load()
        paparazzi.snapshot { BaupilotAppContent(state = AppNavigationState(), onStateChange = {}, data = data) }
    }
}

/** Login while logged out, compared with `design/reference/android/Login_dark.png`. */
class LoginDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Login", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun login() {
        val data = TestAppData.load()
        paparazzi.snapshot { BaupilotAppContent(state = AppNavigationState(), onStateChange = {}, data = data) }
    }
}

/** Home tab, logged in, compared with `design/reference/android/Home_light.png`. */
class HomeLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Home", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun home() {
        val data = TestAppData.load()
        paparazzi.snapshot { BaupilotAppContent(state = AppNavigationState().logIn(), onStateChange = {}, data = data) }
    }
}

/** Home tab, logged in, compared with `design/reference/android/Home_dark.png`. */
class HomeDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Home", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun home() {
        val data = TestAppData.load()
        paparazzi.snapshot { BaupilotAppContent(state = AppNavigationState().logIn(), onStateChange = {}, data = data) }
    }
}

/** Zeitplan tab, logged in, compared with `design/reference/android/Zeitplan_light.png`. */
class ZeitplanLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Zeitplan", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun zeitplan() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Schedule)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Zeitplan tab, logged in, compared with `design/reference/android/Zeitplan_dark.png`. */
class ZeitplanDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Zeitplan", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun zeitplan() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Schedule)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Profil tab, logged in, compared with `design/reference/android/Profil_light.png`. */
class ProfilLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Profil", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun profil() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Profile)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Profil tab, logged in, compared with `design/reference/android/Profil_dark.png`. */
class ProfilDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Profil", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun profil() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Profile)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Gewerk-Detail Estrich, opened from Home, compared with `design/reference/android/Gewerk_light.png`. */
class GewerkLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Gewerk", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun gewerk() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().open(AppRoute.Trade("screed"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Gewerk-Detail Estrich, opened from Home, compared with `design/reference/android/Gewerk_dark.png`. */
class GewerkDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Gewerk", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun gewerk() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().open(AppRoute.Trade("screed"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Dokumente tab, logged in, compared with `design/reference/android/Dokumente_light.png`. */
class DokumenteLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Dokumente", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun dokumente() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Documents)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Dokumente tab, logged in, compared with `design/reference/android/Dokumente_dark.png`. */
class DokumenteDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Dokumente", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun dokumente() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Documents)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Dokument ansehen (Angebot Wagner), opened from Dokumente, compared with `design/reference/android/DokViewer_light.png`. */
class DokViewerLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("DokViewer", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun dokViewer() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Documents).open(AppRoute.Document("doc-offer-wagner"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Dokument ansehen (Angebot Wagner), opened from Dokumente, compared with `design/reference/android/DokViewer_dark.png`. */
class DokViewerDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("DokViewer", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun dokViewer() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Documents).open(AppRoute.Document("doc-offer-wagner"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Angebote vergleichen (Estrich), Home → Gewerk → Vergleich, compared with `design/reference/android/Vergleich_light.png`. */
class VergleichLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Vergleich", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun vergleich() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().open(AppRoute.Trade("screed")).open(AppRoute.Compare("screed"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Angebote vergleichen (Estrich), Home → Gewerk → Vergleich, compared with `design/reference/android/Vergleich_dark.png`. */
class VergleichDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Vergleich", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun vergleich() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().open(AppRoute.Trade("screed")).open(AppRoute.Compare("screed"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Mitteilungen, Home → Mitteilungen, compared with `design/reference/android/Mitteilungen_light.png`. */
class MitteilungenLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Mitteilungen", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun mitteilungen() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().open(AppRoute.Notifications)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Mitteilungen, Home → Mitteilungen, compared with `design/reference/android/Mitteilungen_dark.png`. */
class MitteilungenDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Mitteilungen", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun mitteilungen() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().open(AppRoute.Notifications)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Gewerke view, logged in, compared with `design/reference/android/AbnGewerke_light.png`. */
class AbnGewerkeLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnGewerke", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun abnGewerke() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Gewerke view, logged in, compared with `design/reference/android/AbnGewerke_dark.png`. */
class AbnGewerkeDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnGewerke", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun abnGewerke() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Fenster – Elemente (D7: Abnahmen tab, then `inspection/windows`), compared with `design/reference/android/AbnElemente_light.png`. */
class AbnElementeLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnElemente", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun abnElemente() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("windows"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Fenster – Elemente (D7), compared with `design/reference/android/AbnElemente_dark.png`. */
class AbnElementeDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnElemente", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun abnElemente() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("windows"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Prüfschritt F8 (D7: … → `inspection/windows` → `inspection/windows/F8/step/3`), compared with `design/reference/android/AbnPruefung_light.png`. */
class AbnPruefungLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnPruefung", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun abnPruefung() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections)
            .open(AppRoute.Inspection("windows"))
            .open(AppRoute.InspectionStep("windows", "F8", 3))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Prüfschritt F8 (D7: … → `inspection/windows` → `inspection/windows/F8/step/3`), compared with `design/reference/android/AbnPruefung_dark.png`. */
class AbnPruefungDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnPruefung", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun abnPruefung() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections)
            .open(AppRoute.Inspection("windows"))
            .open(AppRoute.InspectionStep("windows", "F8", 3))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Mangel erfassen (D7: … → `inspection/windows` → Prüfschritt F8 → `defect/new`), compared with `design/reference/android/AbnMangel_light.png`. */
class AbnMangelLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnMangel", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun abnMangel() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections)
            .open(AppRoute.Inspection("windows"))
            .open(AppRoute.InspectionStep("windows", "F8", 3))
            .open(AppRoute.NewDefect)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Mangel erfassen (D7: … → `inspection/windows` → Prüfschritt F8 → `defect/new`), compared with `design/reference/android/AbnMangel_dark.png`. */
class AbnMangelDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnMangel", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun abnMangel() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections)
            .open(AppRoute.Inspection("windows"))
            .open(AppRoute.InspectionStep("windows", "F8", 3))
            .open(AppRoute.NewDefect)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Abnahme & Unterschrift (D7: … → `inspection/windows` → `inspection/windows/sign`), compared with `design/reference/android/AbnSignatur_light.png`. */
class AbnSignaturLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnSignatur", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun abnSignatur() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections)
            .open(AppRoute.Inspection("windows"))
            .open(AppRoute.SignOff("windows"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Abnahme & Unterschrift (D7: … → `inspection/windows` → `inspection/windows/sign`), compared with `design/reference/android/AbnSignatur_dark.png`. */
class AbnSignaturDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnSignatur", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun abnSignatur() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections)
            .open(AppRoute.Inspection("windows"))
            .open(AppRoute.SignOff("windows"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Offene Mängel (D7: `logIn` then `go(Defects)`), compared with `design/reference/android/AbnMaengel_light.png`. */
class AbnMaengelLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnMaengel", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun abnMaengel() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().go(NavigationTarget.Defects)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Offene Mängel (D7: `logIn` then `go(Defects)`), compared with `design/reference/android/AbnMaengel_dark.png`. */
class AbnMaengelDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("AbnMaengel", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun abnMaengel() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().go(NavigationTarget.Defects)
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Erdarbeiten – Protokoll (D10: `logIn`, `select(Inspections)`, `open(Inspection("earthworks"))`), compared with `design/reference/android/Erd_light.png`. */
class ErdLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Erd", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun erd() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("earthworks"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Erdarbeiten – Protokoll (D10: `logIn`, `select(Inspections)`, `open(Inspection("earthworks"))`), compared with `design/reference/android/Erd_dark.png`. */
class ErdDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Erd", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun erd() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("earthworks"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Rohbau – Übersicht (D10: `logIn`, `select(Inspections)`, `open(Inspection("shell"))`), compared with `design/reference/android/Rohbau_light.png`. */
class RohbauLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Rohbau", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun rohbau() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("shell"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Rohbau – Übersicht (D10: `logIn`, `select(Inspections)`, `open(Inspection("shell"))`), compared with `design/reference/android/Rohbau_dark.png`. */
class RohbauDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Rohbau", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun rohbau() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("shell"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Rohbau – Maße & Lot (D10: `logIn`, `select(Inspections)`, `open(Inspection("shell"))`, `open(InspectionCheck("shell", "measure"))`), compared with `design/reference/android/RohbauMasse_light.png`. */
class RohbauMasseLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("RohbauMasse", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun rohbauMasse() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("shell"))
            .open(AppRoute.InspectionCheck("shell", "measure"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Rohbau – Maße & Lot (D10: `logIn`, `select(Inspections)`, `open(Inspection("shell"))`, `open(InspectionCheck("shell", "measure"))`), compared with `design/reference/android/RohbauMasse_dark.png`. */
class RohbauMasseDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("RohbauMasse", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun rohbauMasse() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("shell"))
            .open(AppRoute.InspectionCheck("shell", "measure"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Zimmerer – Dachstuhl (D10: `logIn`, `select(Inspections)`, `open(Inspection("carpentry"))`), compared with `design/reference/android/Zimmerer_light.png`. */
class ZimmererLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Zimmerer", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun zimmerer() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("carpentry"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Zimmerer – Dachstuhl (D10: `logIn`, `select(Inspections)`, `open(Inspection("carpentry"))`), compared with `design/reference/android/Zimmerer_dark.png`. */
class ZimmererDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("Zimmerer", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun zimmerer() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("carpentry"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Elektro – Übersicht (D10: `logIn`, `select(Inspections)`, `open(Inspection("electrical"))`), compared with `design/reference/android/ElUebersicht_light.png`. */
class ElUebersichtLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("ElUebersicht", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun elUebersicht() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}


/** Abnahmen, Elektro – Übersicht (D10: `logIn`, `select(Inspections)`, `open(Inspection("electrical"))`), compared with `design/reference/android/ElUebersicht_dark.png`. */
class ElUebersichtDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("ElUebersicht", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun elUebersicht() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Elektro – Raumprüfung Küche (D10: `logIn`, `select(Inspections)`, `open(Inspection("electrical"))`, `open(InspectionRoom("electrical", "kitchen"))`), compared with `design/reference/android/ElRaum_light.png`. */
class ElRaumLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("ElRaum", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun elRaum() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
            .open(AppRoute.InspectionRoom("electrical", "kitchen"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Elektro – Raumprüfung Küche (D10: `logIn`, `select(Inspections)`, `open(Inspection("electrical"))`, `open(InspectionRoom("electrical", "kitchen"))`), compared with `design/reference/android/ElRaum_dark.png`. */
class ElRaumDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("ElRaum", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun elRaum() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
            .open(AppRoute.InspectionRoom("electrical", "kitchen"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Elektro – Zählerschrank & FI-Test (D10: `logIn`, `select(Inspections)`, `open(Inspection("electrical"))`, `open(InspectionCheck("electrical", "panel"))`), compared with `design/reference/android/ElVerteiler_light.png`. */
class ElVerteilerLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("ElVerteiler", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun elVerteiler() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
            .open(AppRoute.InspectionCheck("electrical", "panel"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Elektro – Zählerschrank & FI-Test (D10: `logIn`, `select(Inspections)`, `open(Inspection("electrical"))`, `open(InspectionCheck("electrical", "panel"))`), compared with `design/reference/android/ElVerteiler_dark.png`. */
class ElVerteilerDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("ElVerteiler", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun elVerteiler() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
            .open(AppRoute.InspectionCheck("electrical", "panel"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Elektro – Rauchwarnmelder (D10: `logIn`, `select(Inspections)`, `open(Inspection("electrical"))`, `open(InspectionCheck("electrical", "smoke"))`), compared with `design/reference/android/ElRauchmelder_light.png`. */
class ElRauchmelderLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("ElRauchmelder", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun elRauchmelder() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
            .open(AppRoute.InspectionCheck("electrical", "smoke"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Elektro – Rauchwarnmelder (D10: `logIn`, `select(Inspections)`, `open(Inspection("electrical"))`, `open(InspectionCheck("electrical", "smoke"))`), compared with `design/reference/android/ElRauchmelder_dark.png`. */
class ElRauchmelderDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("ElRauchmelder", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun elRauchmelder() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("electrical"))
            .open(AppRoute.InspectionCheck("electrical", "smoke"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Sanitär – Übersicht (D10: `logIn`, `select(Inspections)`, `open(Inspection("plumbing"))`), compared with `design/reference/android/SanUebersicht_light.png`. */
class SanUebersichtLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("SanUebersicht", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun sanUebersicht() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Sanitär – Übersicht (D10: `logIn`, `select(Inspections)`, `open(Inspection("plumbing"))`), compared with `design/reference/android/SanUebersicht_dark.png`. */
class SanUebersichtDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("SanUebersicht", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun sanUebersicht() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Gefälle Abwasser (D10: `logIn`, `select(Inspections)`, `open(Inspection("plumbing"))`, `open(InspectionCheckItem("plumbing", "slope", "A3"))`), compared with `design/reference/android/SanGefaelle_light.png`. */
class SanGefaelleLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("SanGefaelle", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun sanGefaelle() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
            .open(AppRoute.InspectionCheckItem("plumbing", "slope", "A3"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Gefälle Abwasser (D10, see the light test), compared with `design/reference/android/SanGefaelle_dark.png`. */
class SanGefaelleDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("SanGefaelle", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun sanGefaelle() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
            .open(AppRoute.InspectionCheckItem("plumbing", "slope", "A3"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Druckprobe Trinkwasser (D10: `logIn`, `select(Inspections)`, `open(Inspection("plumbing"))`, `open(InspectionCheck("plumbing", "pressure"))`), compared with `design/reference/android/SanDruck_light.png`. */
class SanDruckLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("SanDruck", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun sanDruck() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
            .open(AppRoute.InspectionCheck("plumbing", "pressure"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Druckprobe Trinkwasser (D10, see the light test), compared with `design/reference/android/SanDruck_dark.png`. */
class SanDruckDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("SanDruck", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun sanDruck() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
            .open(AppRoute.InspectionCheck("plumbing", "pressure"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Bad OG Objekte (D10: `logIn`, `select(Inspections)`, `open(Inspection("plumbing"))`, `open(InspectionRoom("plumbing", "bathUpper"))`), compared with `design/reference/android/SanBad_light.png`. */
class SanBadLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("SanBad", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun sanBad() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
            .open(AppRoute.InspectionRoom("plumbing", "bathUpper"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Bad OG Objekte (D10: `logIn`, `select(Inspections)`, `open(Inspection("plumbing"))`, `open(InspectionRoom("plumbing", "bathUpper"))`), compared with `design/reference/android/SanBad_dark.png`. */
class SanBadDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("SanBad", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun sanBad() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("plumbing"))
            .open(AppRoute.InspectionRoom("plumbing", "bathUpper"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Estrich – Übersicht (D10: `logIn`, `select(Inspections)`, `open(Inspection("screed"))`), compared with `design/reference/android/EsUebersicht_light.png`. */
class EsUebersichtLightSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("EsUebersicht", SnapshotMode.LIGHT), SnapshotMode.LIGHT)

    @Test
    fun esUebersicht() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("screed"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}

/** Abnahmen, Estrich – Übersicht (D10: `logIn`, `select(Inspections)`, `open(Inspection("screed"))`), compared with `design/reference/android/EsUebersicht_dark.png`. */
class EsUebersichtDarkSnapshotTest {
    @get:Rule
    val paparazzi = baupilotPaparazzi(ReferenceSnapshotHandler("EsUebersicht", SnapshotMode.DARK), SnapshotMode.DARK)

    @Test
    fun esUebersicht() {
        val data = TestAppData.load()
        val state = AppNavigationState().logIn().select(AppTab.Inspections).open(AppRoute.Inspection("screed"))
        paparazzi.snapshot { BaupilotAppContent(state = state, onStateChange = {}, data = data) }
    }
}
