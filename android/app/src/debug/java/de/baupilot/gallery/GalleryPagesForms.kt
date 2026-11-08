package de.baupilot.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.BaupilotOutlinedTextField
import de.baupilot.designsystem.components.ButtonSize
import de.baupilot.designsystem.components.FieldLook
import de.baupilot.designsystem.components.FilledButton
import de.baupilot.designsystem.components.FilledContainer
import de.baupilot.designsystem.components.GoogleMark
import de.baupilot.designsystem.components.OutlinedButton
import de.baupilot.designsystem.components.TextButton
import de.baupilot.designsystem.components.TextButtonSize
import de.baupilot.designsystem.components.TonalButton

/** Only the parts the Login screen added: the outlined field and the button extensions. */
@Composable
internal fun FormsPage() {
    Caption("OutlinedTextField · Focused, Platzhalter")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BaupilotOutlinedTextField(
            label = "E-Mail",
            value = "",
            onValueChange = {},
            placeholder = "name@beispiel.de",
            look = FieldLook.Focused,
        )
        Caption("OutlinedTextField · Idle, Passwort-Platzhalter")
        BaupilotOutlinedTextField(
            label = "Passwort",
            value = "",
            onValueChange = {},
            placeholder = "••••••••",
            isPassword = true,
        )
        Caption("OutlinedTextField · Idle mit Wert, Passwort verdeckt")
        BaupilotOutlinedTextField(
            label = "Passwort",
            value = "geheim",
            onValueChange = {},
            placeholder = "••••••••",
            isPassword = true,
        )
    }
    Caption("FilledButton · letterSpacing 0.1")
    FilledButton("Anmelden", ButtonSize.Regular, FilledContainer.Primary, letterSpacing = 0.1.sp)
    Caption("OutlinedButton · leading GoogleMark")
    OutlinedButton("Mit Google anmelden", contentColor = Tokens.text, leading = { GoogleMark() })
    Caption("GoogleMark")
    GoogleMark()
    Caption("TonalButton · TextButton Compact40 · Wide44")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        TonalButton("Bearbeiten")
        TextButton("Alle", size = TextButtonSize.Compact40)
    }
    TextButton("Neues Bauvorhaben anlegen", modifier = Modifier.fillMaxWidth(), size = TextButtonSize.Wide44)
}
