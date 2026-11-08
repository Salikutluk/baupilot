#if DEBUG
import SwiftUI

/// Step rows, the current step card, progress and callouts.
struct GalleryStepsPage: View {
    var body: some View {
        GalleryGroup("StepRow · done (meta) / open / open with tag") {
            VStack(alignment: .leading, spacing: 8) {
                StepRow(
                    number: 1, title: "Drehstellung: Flügel bleibt stehen", state: .done,
                    meta: .text("1 Foto"))
                StepRow(number: 4, title: "Schließen & Verriegeln, Griff leichtgängig", state: .open())
                StepRow(
                    number: 5, title: "Glas, Rahmen & Beschläge unbeschädigt",
                    state: .open(tag: "Pflicht"))
            }
        }
        GalleryGroup("CurrentStepCard · AbnPruefung") {
            CurrentStepCard(number: 3, title: "Sitzt das Fenster im Wasser?") {
                CurrentStepCard<EmptyView>.body(
                    "Wasserwaage oben und seitlich an den Rahmen halten – die Blase muss jeweils mittig stehen.")
                HStack(spacing: 8) {
                    PhotoTile(kind: .guide(title: "So prüfen", illustration: guideIllustration), height: .h84)
                    PhotoTile(kind: .photo(photo, caption: "Foto oben", checked: true), height: .h84)
                    PhotoTile(kind: .add(label: "Foto seitlich"), height: .h84)
                }
            }
        }
        GalleryGroup("ProgressBar · phase 45 % / card 36 %") {
            VStack(spacing: 8) {
                ProgressBar(value: 0.45, style: .phase)
                ProgressBar(value: 0.36, style: .card)
            }
        }
        GalleryGroup("InfoCallout · warn / neutralSurface / locked / accent") {
            VStack(spacing: 6) {
                InfoCallout(
                    text: "Ohne CM-Messung und Aufheizprotokoll keinen Boden verlegen lassen.",
                    icon: .alertTriangleW22, tone: .warn)
                InfoCallout(
                    text: "Als Nächstes Dusche: 2 Minuten voll aufdrehen – der Ablauf muss alles schlucken, kein Wasser darf stehen bleiben.",
                    icon: .infoW22, tone: .neutralSurface)
                InfoCallout(
                    text: "Bodenbelag erst nach Freigabe", icon: .lock, tone: .locked, textSize: 14)
                InfoCallout(
                    text: "Hahn enthält das Belegreifheizen (ca. 600 €). Rechnet man es heraus, liegen beide nur rund 50 € auseinander.",
                    icon: .infoW22, tone: .accent, radius: .r14, textSize: 14)
            }
        }
    }

    private var photo: Image { Image(systemName: "photo") }

    private var guideIllustration: AnyView {
        AnyView(Icon(.clipboardW22, size: 28, color: Tokens.muted.color))
    }
}
#endif
