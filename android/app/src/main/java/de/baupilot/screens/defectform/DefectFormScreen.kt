package de.baupilot.screens.defectform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.baupilot.app.DefectFormModel
import de.baupilot.designsystem.BaupilotIcon
import de.baupilot.designsystem.BaupilotText
import de.baupilot.designsystem.Tokens
import de.baupilot.designsystem.components.Action
import de.baupilot.designsystem.components.AlertBanner
import de.baupilot.designsystem.components.BarPadding
import de.baupilot.designsystem.components.BaupilotListItem
import de.baupilot.designsystem.components.BaupilotOutlinedTextArea
import de.baupilot.designsystem.components.BaupilotOutlinedTextField
import de.baupilot.designsystem.components.BaupilotSwitch
import de.baupilot.designsystem.components.BaupilotTopAppBar
import de.baupilot.designsystem.components.DetailScreenScaffold
import de.baupilot.designsystem.components.FieldLook
import de.baupilot.designsystem.components.FilterChip
import de.baupilot.designsystem.components.ListItemHeight
import de.baupilot.designsystem.components.Nav
import de.baupilot.designsystem.components.PhotoTile
import de.baupilot.designsystem.components.PhotoTileKind
import de.baupilot.designsystem.components.SegmentedButtons
import de.baupilot.designsystem.components.TitleStyle
import de.baupilot.designsystem.components.Trailing

/** Actions the Mangel form raises; the shell turns them into navigation. */
sealed interface DefectFormAction {
    data object Cancel : DefectFormAction
    data class Save(val tradeId: String) : DefectFormAction
}

/**
 * Mangel erfassen (Nr. 13, AndAbnMangel.dc.html): modal bar with the close button and "Sichern", the alert banner,
 * the description field, the photo row, the severity buttons, the deadline field with its chips and the two form
 * rows. The form is local state (D9): it survives rotation and the shell's saveable entry, and nothing is written
 * anywhere (I8). "Foto aufnehmen" has no action; "Sichern" and the close button only raise [onAction].
 */
@Composable
fun DefectFormScreen(
    model: DefectFormModel,
    modifier: Modifier = Modifier,
    onAction: (DefectFormAction) -> Unit = {},
) {
    var description by rememberSaveable { mutableStateOf(model.description) }
    var severity by rememberSaveable { mutableStateOf(model.initialSeverity) }
    var deadline by rememberSaveable { mutableStateOf(model.initialDeadline) }
    var date by rememberSaveable { mutableStateOf(model.deadlineText(model.initialDeadline).orEmpty()) }
    var notify by rememberSaveable { mutableStateOf(model.notifyInitial) }

    DetailScreenScaffold(
        topBar = {
            BaupilotTopAppBar(
                title = model.title,
                padding = BarPadding.Modal,
                navigation = Nav.Close(label = "Abbrechen", onClick = { onAction(DefectFormAction.Cancel) }),
                titleStyle = TitleStyle.Detail,
                actions = listOf(
                    Action.Text(
                        label = model.saveLabel,
                        height = 40,
                        onClick = { onAction(DefectFormAction.Save(model.tradeId)) },
                        testTag = "defect.save",
                    ),
                ),
            )
        },
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
        spacing = 18.dp,
        modifier = modifier.testTag("screen.defect.new"),
    ) {
        AlertBanner(icon = BaupilotIcon.AlertTriangleW22, title = model.context, subtitle = model.finding)
        BaupilotOutlinedTextArea(label = model.descriptionLabel, value = description, onValueChange = { description = it })
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel(model.photosLabel)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                model.photoLabels.forEach { label ->
                    PhotoTile(
                        kind = PhotoTileKind.Icon(BaupilotIcon.Image),
                        height = 76.dp,
                        modifier = Modifier.width(76.dp),
                        contentDescription = label,
                    )
                }
                PhotoTile(
                    kind = PhotoTileKind.Add(null),
                    height = 76.dp,
                    modifier = Modifier.width(76.dp),
                    contentDescription = model.addPhotoLabel,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel(model.severityLabel)
            SegmentedButtons(
                items = model.severities,
                selectedIndex = severity,
                selectedPrefix = model.selectedPrefix,
                identifiers = model.severities.indices.map { "defect.severity.$it" },
                onSelect = { severity = it },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BaupilotOutlinedTextField(
                label = model.deadlineLabel,
                value = date,
                onValueChange = { date = it },
                placeholder = "",
                look = FieldLook.Idle,
                trailingIcon = BaupilotIcon.Calendar,
                fieldModifier = Modifier.testTag("defect.deadline.value"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                model.deadlineOptions.forEachIndexed { index, option ->
                    FilterChip(
                        label = option.label,
                        selected = index == deadline,
                        modifier = Modifier.testTag("defect.deadline.$index"),
                        onClick = {
                            model.deadlineText(index)?.let {
                                deadline = index
                                date = it
                            }
                        },
                    )
                }
            }
        }
        Column {
            BaupilotListItem(
                title = model.responsibleLabel,
                subtitle = model.responsible,
                minHeight = ListItemHeight.H56,
                horizontalPadding = 0.dp,
            )
            BaupilotListItem(
                title = model.notifyLabel,
                minHeight = ListItemHeight.H56,
                horizontalPadding = 0.dp,
                trailing = Trailing.Switch {
                    BaupilotSwitch(
                        checked = notify,
                        contentDescription = model.notifyLabel,
                        onCheckedChange = { notify = it },
                        modifier = Modifier.testTag("defect.notify"),
                    )
                },
            )
        }
    }
}

/** Field label above a group (AndAbnMangel.dc.html:41, :56): 14 / 500 `muted`. */
@Composable
private fun FieldLabel(text: String) {
    BaupilotText(text = text, size = 14.sp, weight = 500, color = Tokens.muted)
}
