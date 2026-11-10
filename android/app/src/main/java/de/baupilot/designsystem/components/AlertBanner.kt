package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.ColorToken
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.wholeDpCentered

/**
 * Alert banner (AndAbnMangel.dc.html:27-33): radius 12, padding 12 16, gap 12, a 20 [icon] and the 15 / 500 [title]
 * above the 13 [subtitle] (gap 2). [background] and [foreground] are the `errBg` / `errFg` pair. A null [icon] or
 * [subtitle] hides its slot (the Maße & Lot result is the title alone).
 */
@Composable
fun AlertBanner(
    icon: BaupilotIcon?,
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    background: ColorToken = Tokens.errBg,
    foreground: ColorToken = Tokens.errFg,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(background.color(), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) Icon(icon, 20.dp, foreground.color())
        Column(Modifier.weight(1f).then(wholeDpCentered()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BaupilotText(text = title, size = 15.sp, weight = 500, color = foreground)
            if (subtitle != null) BaupilotText(text = subtitle, size = 13.sp, color = foreground)
        }
    }
}
