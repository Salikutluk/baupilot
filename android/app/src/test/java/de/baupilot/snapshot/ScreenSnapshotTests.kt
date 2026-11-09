package de.baupilot.snapshot

import de.baupilot.app.AppNavigationState
import de.baupilot.app.AppRoute
import de.baupilot.app.AppTab
import de.baupilot.app.BaupilotAppContent
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
