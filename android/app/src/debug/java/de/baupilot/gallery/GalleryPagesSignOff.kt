package de.baupilot.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.CaptionedActionBar
import de.baupilot.designsystem.components.CountTile
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.SignatureCard
import de.baupilot.designsystem.components.SignatureCardStyle
import de.baupilot.inspection.Extent
import de.baupilot.inspection.Point
import de.baupilot.inspection.SignatureStrokes

@Composable
internal fun SignOffPage() {
    var drawn by remember {
        mutableStateOf(
            SignatureStrokes(Extent(358f, 104f))
                .begin(Point(40f, 70f)).extend(Point(90f, 30f)).extend(Point(140f, 70f)).extend(Point(190f, 30f)).end()
                .begin(Point(250f, 60f)).end(),
        )
    }
    Caption("CountTile · drei Spalten à 114, Lücke 8, Radius 12, Innenabstand 12, 24 / 500 + 13 (AndAbnSignatur:26-29)")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CountTile("12", "geprüft", Tokens.surfaceContainerHigh, Tokens.text, Modifier.weight(1f))
        CountTile("11", "in Ordnung", Tokens.okBg, Tokens.okFg, Modifier.weight(1f))
        CountTile("1", "Mangel", Tokens.errBg, Tokens.errFg, Modifier.weight(1f))
    }
    Caption("SignatureCard · Signed · Rand 1 outlineVariant, Radius 12, Unterschrift 120 × 48, Strich 2 (AndAbnSignatur:54-59)")
    SignatureCard(
        SignatureCardStyle.Signed("Handwerker", "Fensterbau Weber", "Unterschrieben 14:32", "Unterschrift Handwerker"),
    )
    Caption("SignatureCard · Pending leer · 104 hoch, Rand 2 gestrichelt primary, Platzhalter, 1-Linie (AndAbnSignatur:60-64)")
    SignatureCard(
        SignatureCardStyle.Pending("Bauherr · Max Mustermann", "Zum Unterschreiben tippen", SignatureStrokes(Extent(358f, 104f)), {}),
    )
    Caption("SignatureCard · Pending mit Strichen · Platzhalter ausgeblendet, Strich 2 rund (D10)")
    SignatureCard(
        SignatureCardStyle.Pending("Bauherr · Max Mustermann", "Zum Unterschreiben tippen", drawn) { drawn = it },
    )
    Caption("CaptionedActionBar · Innenabstand 12 16 24, Lücke 6, Hinweis 12 muted zentriert (AndAbnSignatur:69-72)")
    CaptionedActionBar(caption = "PDF mit Fotos geht an Sie und den Handwerker") {
        FilledButton("Protokoll abschließen", ButtonSize.Regular, FilledContainer.Primary)
    }
}
