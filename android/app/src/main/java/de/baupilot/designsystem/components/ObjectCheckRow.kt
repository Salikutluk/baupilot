package de.baupilot.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Icon
import de.baupilot.designsystem.Tokens

/** Test tags of the two cells of a [VerdictToggle]. */
data class ObjectCheckTags(val ok: String, val defect: String)

/**
 * Check row of the bathroom objects (AndSanBad:46-62): at least 64 tall, plus 1 `outlineVariant` divider above it when
 * [showsDivider]; the 15 [title] over the 13 / 18 `muted` [hint] (gap 2) and the [VerdictToggle] at the end, gap 12.
 */
@Composable
fun ObjectCheckRow(
    title: String,
    hint: String,
    ok: Boolean,
    showsDivider: Boolean,
    modifier: Modifier = Modifier,
    tags: ObjectCheckTags? = null,
) {
    Column(modifier.fillMaxWidth()) {
        if (showsDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(Tokens.outlineVariant.color()))
        Row(
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                BaupilotText(text = title, size = 15.sp, color = Tokens.text)
                BaupilotText(text = hint, size = 13.sp, lineHeight = 18.sp, color = Tokens.muted)
            }
            VerdictToggle(
                ok = ok,
                tags = tags,
                defectDescription = "$title: Mangel",
                okDescription = "$title: in Ordnung",
            )
        }
    }
}

/**
 * The defect / ok pair (AndSanBad:55-62): a 1 `outline` border, radius 20 and clipped, 40 inside (42 outside) and 98
 * wide. The defect cell is 48 wide with a 1 `outline` line on its right edge and a `muted` cross; the ok cell is 48
 * wide and is `okBg` with an `okFg` check when [ok]. Neither cell acts; they are buttons for accessibility. The cells
 * stay 40 tall in layout: Compose widens each one's hit area to 48 without changing its size.
 */
@Composable
fun VerdictToggle(
    ok: Boolean,
    modifier: Modifier = Modifier,
    tags: ObjectCheckTags? = null,
    defectDescription: String = "Mangel",
    okDescription: String = "in Ordnung",
) {
    val outline = Tokens.outline.color()
    Row(
        modifier = modifier
            .size(width = 98.dp, height = 42.dp)
            .border(1.dp, outline, RoundedCornerShape(20.dp))
            .padding(1.dp)
            .clip(RoundedCornerShape(19.dp)),
    ) {
        VerdictCell(
            icon = BaupilotIcon.XW26,
            tint = Tokens.muted.color(),
            description = defectDescription,
            tag = tags?.defect,
            modifier = Modifier.drawBehind {
                val line = 1.dp.toPx()
                drawRect(outline, Offset(size.width - line, 0f), Size(line, size.height))
            },
        )
        VerdictCell(
            icon = BaupilotIcon.CheckW30,
            tint = (if (ok) Tokens.okFg else Tokens.muted).color(),
            description = okDescription,
            tag = tags?.ok,
            modifier = if (ok) Modifier.background(Tokens.okBg.color()) else Modifier,
        )
    }
}

@Composable
private fun VerdictCell(
    icon: BaupilotIcon,
    tint: androidx.compose.ui.graphics.Color,
    description: String,
    tag: String?,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .clickable(role = Role.Button, onClick = {})
            .semantics { contentDescription = description }
            .then(if (tag != null) Modifier.testTag(tag) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, 18.dp, tint)
    }
}
