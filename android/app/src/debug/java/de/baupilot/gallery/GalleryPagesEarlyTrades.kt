package de.baupilot.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.components.BaupilotCard
import de.baupilot.designsystem.components.BaupilotVerticalStepper
import de.baupilot.designsystem.components.CheckHintRow
import de.baupilot.designsystem.components.InfoCallout
import de.baupilot.designsystem.components.NumberedList
import de.baupilot.designsystem.components.SocketGrid
import de.baupilot.designsystem.components.SocketItem
import de.baupilot.designsystem.components.SocketTileStyle
import de.baupilot.designsystem.components.StatusList
import de.baupilot.designsystem.components.StatusRow
import de.baupilot.designsystem.components.Step
import de.baupilot.designsystem.components.StepState
import de.baupilot.designsystem.components.StepperVariant
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.AlertBanner
import de.baupilot.designsystem.components.BannerTrailing
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.PhaseCard
import de.baupilot.designsystem.components.PhaseCardState
import de.baupilot.designsystem.components.ProtocolBanner
import de.baupilot.designsystem.components.StatusChip
import de.baupilot.designsystem.components.StatusChipColors
import de.baupilot.designsystem.components.StatusChipSize
import de.baupilot.designsystem.components.Trailing
import de.baupilot.designsystem.components.ValueStepper

@Composable
internal fun ProtocolPage() {
    Caption("ProtocolBanner · Dokument-Button (AndErd:25)")
    ProtocolBanner(
        icon = BaupilotIcon.CheckCircleW24,
        title = "Abgenommen am 14.04.2026",
        text = "Unterschrieben · keine Mängel",
        background = Tokens.okBg,
        foreground = Tokens.okFg,
        trailing = BannerTrailing.Document("PDF"),
    )
    Caption("ProtocolBanner · Chevron")
    ProtocolBanner(
        icon = BaupilotIcon.AlertTriangleW22,
        title = "1 Mangel offen",
        text = "Bewehrung Fundament",
        background = Tokens.warnBg,
        foreground = Tokens.warnFg,
        trailing = BannerTrailing.Chevron,
    )
    Caption("ProtocolBanner · ohne Trailing")
    ProtocolBanner(
        icon = BaupilotIcon.CheckCircleW24,
        title = "Abgenommen am 14.04.2026",
        text = "Unterschrieben · keine Mängel",
        background = Tokens.okBg,
        foreground = Tokens.okFg,
    )
    Caption("ListItem · titleSize 15 / subtitleSize 13")
    BaupilotListItem(
        title = "Baugrube ausgehoben",
        subtitle = "12.04.2026",
        leading = Leading.Icon(BaupilotIcon.CheckCircleW24, Tokens.ok),
        trailing = Trailing.Text("12.04.", 13.sp, Tokens.muted, 400),
        minHeight = ListItemHeight.H48,
        titleSize = 15.sp,
        subtitleSize = 13.sp,
    )
}

@Composable
internal fun MeasurePage() {
    Caption("ValueStepper · 50 hoch, 150 breit (AndRohbauMasse:38)")
    ValueStepper("Breite", "1005", onDecrement = null, onIncrement = null)
    Caption("StatusChip H32Body · F1–F7, Row(spacedBy 8)")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("F1", "F2", "F3", "F4", "F5", "F6", "F7").forEach {
            StatusChip(it, StatusChipColors.Ok(), StatusChipSize.H32Body)
        }
    }
    Caption("AlertBanner · nur Text, ok und Fehler")
    AlertBanner(
        icon = null, title = "Innerhalb der Toleranz", subtitle = null,
        background = Tokens.okBg, foreground = Tokens.okFg,
    )
    AlertBanner(icon = null, title = "Außerhalb der Toleranz", subtitle = null)
}

@Composable
internal fun ElectricalPage() {
    Caption("PhaseCard · Done und Active, gleiche Höhe, 2 × 175 (AndElUebersicht:30)")
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PhaseCard(
            "1 · Rohinstallation", "Abgenommen 29.01.",
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        PhaseCard(
            "2 · Fertiginstallation", "4 von 11 geprüft",
            PhaseCardState.Active(0.36f, Tokens.track.elUebersicht),
            Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

/** Elektro step parts, first page: SocketGrid, annotated progress line, NumberedList, StatusList, warn InfoCallout. */
@Composable
internal fun ElectricalStepsPage() {
    val ok = SocketTileStyle(Tokens.okBg, Tokens.okFg)
    val err = SocketTileStyle(Tokens.errBg, Tokens.errFg)
    val now = SocketTileStyle(Tokens.nowBg, Tokens.nowFg, Tokens.primary)
    val open = SocketTileStyle(Tokens.openBg, Tokens.muted)
    fun socket(number: Int, label: String, style: SocketTileStyle) =
        SocketItem("S$number", label, style, "Steckdose S$number: $label")
    Caption("SocketGrid · 4 Spalten, Kacheln 52 hoch, Abstand 8 (AndElRaum:48)")
    SocketGrid(
        listOf(
            socket(1, "korrekt", ok), socket(2, "korrekt", ok), socket(3, "korrekt", ok), socket(4, "Fehler", err),
            socket(5, "korrekt", ok), socket(6, "jetzt", now), socket(7, "offen", open), socket(8, "offen", open),
        ),
    )
    Caption("BaupilotText · AnnotatedString")
    BaupilotText(
        text = buildAnnotatedString {
            append("5 von 8 getestet · ")
            withStyle(SpanStyle(color = Tokens.err.color(), fontWeight = FontWeight.Medium)) {
                append("S4: Mangel erfasst")
            }
        },
        size = 13.sp,
        color = Tokens.muted,
    )
    Caption("NumberedList · 14 / 20, Abstand 4, Einzug 20")
    NumberedList(
        listOf(
            "Prüftaste „T“ am FI-Schalter drücken.",
            "Der Hebel muss sofort nach unten springen.",
            "Hebel wieder nach oben – Strom ist zurück.",
        ),
        color = Tokens.muted,
    )
    Caption("StatusList · Zeilen mindestens 56, Trenner outlineVariant")
    StatusList(
        listOf(
            StatusRow("FI 1", "Bad und Küche", "Ausgelöst", StatusChipColors.Ok()),
            StatusRow("FI 2", "Wohnen und Flur", "Jetzt testen", StatusChipColors.Pending),
        ),
        background = Tokens.bg,
        divider = Tokens.outlineVariant,
    )
    Caption("InfoCallout · warnBg / warnFg, BoltW22")
    InfoCallout(
        "Nur von außen prüfen. Keine Abdeckungen abschrauben – das macht der Elektriker.",
        icon = BaupilotIcon.BoltW22,
        container = Tokens.warnBg,
        content = Tokens.warnFg,
    )
}

/** Elektro step parts, second page: CheckHintRow in a primaryContainer card, Leading.Dot rows, Step.metaColor. */
@Composable
internal fun ElectricalSteps2Page() {
    Caption("CheckHintRow · erledigt und offen, Hinweis 13 / 18")
    BaupilotCard(container = Tokens.primaryContainer) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CheckHintRow(
                "Prüftaste drücken", "Taste am Melder gedrückt halten, bis der Signalton kommt.", done = true,
                border = Tokens.onPrimaryContainer, fill = Tokens.onPrimaryContainer, tick = Tokens.primaryContainer,
            )
            CheckHintRow(
                "Melder piept laut", "Lautstärke und Dauer ohne Unterbrechung.", done = false,
                border = Tokens.onPrimaryContainer, fill = Tokens.onPrimaryContainer, tick = Tokens.primaryContainer,
            )
        }
    }
    Caption("ListItem · Leading.Dot, 48 hoch")
    Column {
        BaupilotListItem(
            "Schlafzimmer", leading = Leading.Dot(Tokens.ok), minHeight = ListItemHeight.H48, horizontalPadding = 0.dp,
            titleSize = 16.sp, trailing = Trailing.Text("Geprüft", 13.sp, Tokens.okFg, 500),
        )
        BaupilotListItem(
            "Flur OG", leading = Leading.Dot(Tokens.primary), minHeight = ListItemHeight.H48, horizontalPadding = 0.dp,
            trailing = Trailing.Text("Jetzt", 13.sp, Tokens.primary, 500),
        )
        BaupilotListItem(
            "Kinderzimmer", leading = Leading.Dot(Tokens.outline), minHeight = ListItemHeight.H48, horizontalPadding = 0.dp,
            trailing = Trailing.Text("Offen", 13.sp, Tokens.muted, 500),
        )
    }
    Caption("VerticalStepper Compact · Step.metaColor okFg, Karte füllt die Breite")
    BaupilotVerticalStepper(
        steps = listOf(
            Step(1, "Anzahl & Position laut Plan", StepState.Done, meta = "8 / 8", metaColor = Tokens.okFg),
            Step(2, "FI-Schutzschalter testen", StepState.Current) {
                BaupilotText("Prüftaste „T“ drücken.", size = 14.sp, color = Tokens.muted, lineHeight = 20.sp)
            },
            Step(3, "Abdeckungen gerade", StepState.Open, tag = "Pflicht"),
        ),
        variant = StepperVariant.Compact,
    )
}

