package de.baupilot.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.ProfileModel
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.AvatarSize
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotAvatar
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotSwitch
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.Leading
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.SectionHeader
import de.baupilot.designsystem.components.SectionHeaderStyle
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.TonalButton
import de.baupilot.designsystem.components.Trailing
import de.baupilot.screens.ScreenCopy

private val TOGGLE_SAVER = listSaver<SnapshotStateList<Boolean>, Boolean>(
    save = { it.toList() },
    restore = { it.toMutableStateList() },
)

/**
 * Profil (AndProfil.dc.html): title bar, profile row and three settings sections. The switches flip local,
 * non-persisted state (D16); the settings rows, "Bearbeiten" and "Abmelden" have no action.
 */
@Composable
fun ProfileScreen(model: ProfileModel) {
    // Keyed by the flat row index; starts from the fixture's `checked` value and survives tab switches and
    // activity recreation (M1).
    val toggles = rememberSaveable(saver = TOGGLE_SAVER) {
        model.groups.flatMap { it.rows }.map { it.isToggle && it.checked == true }.toMutableStateList()
    }
    Box(Modifier.fillMaxSize().background(Tokens.bg.color()).testTag("screen.profile")) {
        Column(Modifier.fillMaxSize()) {
            BaupilotTopAppBar(
                title = ScreenCopy.Profil.TITLE,
                padding = BarPadding.TitleOnly,
                titleStyle = TitleStyle.Large,
            )
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                ProfileRow(model)
                var index = 0
                model.groups.forEach { group ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(Tokens.divider.color())
                            .padding(top = 1.dp)
                            .background(Tokens.bg.color())
                            .padding(vertical = 4.dp),
                    ) {
                        SectionHeader(group.title, SectionHeaderStyle.Settings)
                        group.rows.forEach { row ->
                            val position = index++
                            val signOut = row.label == ScreenCopy.Profil.SIGN_OUT
                            BaupilotListItem(
                                title = row.label,
                                subtitle = row.sub?.takeIf { it.isNotEmpty() },
                                leading = Leading.Icon(
                                    ScreenCopy.Profil.ROW_ICONS[position],
                                    if (signOut) Tokens.error else Tokens.muted,
                                ),
                                trailing = if (row.isToggle) {
                                    Trailing.Switch {
                                        BaupilotSwitch(
                                            checked = toggles[position],
                                            contentDescription = row.label,
                                            onCheckedChange = { toggles[position] = it },
                                        )
                                    }
                                } else {
                                    null
                                },
                                minHeight = ListItemHeight.H56,
                                titleColor = if (signOut) Tokens.error else Tokens.text,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(model: ProfileModel) {
    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BaupilotAvatar(model.initials, AvatarSize.S56, Tokens.primaryContainer, Tokens.onPrimaryContainer)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BaupilotText(text = model.name, size = 18.sp, color = Tokens.text)
            BaupilotText(
                text = model.email,
                size = 14.sp,
                color = Tokens.muted,
            )
        }
        TonalButton(ScreenCopy.Profil.EDIT)
    }
}
