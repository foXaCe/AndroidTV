package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.components.VegafoXButton
import org.jellyfin.androidtv.ui.base.components.VegafoXButtonVariant
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors

@Composable
fun SeasonSelectionDialog(
	showName: String,
	numberOfSeasons: Int,
	is4k: Boolean,
	unavailableSeasons: Set<Int>,
	onConfirm: (selectedSeasons: List<Int>) -> Unit,
	onDismiss: () -> Unit,
) {
	val accentColor = VegafoXColors.OrangePrimary

	val checkedStates =
		remember {
			mutableStateListOf<Boolean>().apply {
				for (season in 1..numberOfSeasons) {
					add(season !in unavailableSeasons)
				}
			}
		}

	val availableSeasons =
		remember(numberOfSeasons, unavailableSeasons) {
			(1..numberOfSeasons).filter { it !in unavailableSeasons }
		}

	var selectAllChecked by remember { mutableStateOf(true) }

	fun updateSelectAll() {
		selectAllChecked = availableSeasons.all { checkedStates[it - 1] }
	}

	Dialog(
		onDismissRequest = onDismiss,
		properties = DialogProperties(usePlatformDefaultWidth = false),
	) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier =
					Modifier
						.widthIn(min = 400.dp, max = 600.dp)
						.clip(RoundedCornerShape(16.dp))
						.background(VegafoXColors.Surface)
						.border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
						.padding(horizontal = 24.dp, vertical = 24.dp),
			) {
				// Title
				Text(
					text = stringResource(R.string.jellyseerr_select_seasons),
					style =
						TextStyle(
							fontFamily = BebasNeue,
							fontSize = 22.sp,
							letterSpacing = 2.sp,
							color = VegafoXColors.TextPrimary,
						),
					modifier = Modifier.padding(bottom = 6.dp),
				)

				// Subtitle
				Text(
					text = "$showName ${if (is4k) "(4K)" else "(HD)"}",
					style =
						TextStyle(
							fontSize = 14.sp,
							color = VegafoXColors.TextSecondary,
						),
					modifier = Modifier.padding(bottom = 16.dp),
				)

				// Select All checkbox
				if (availableSeasons.isNotEmpty()) {
					SeasonCheckboxRow(
						label =
							if (unavailableSeasons.isEmpty()) {
								stringResource(R.string.jellyseerr_select_all_seasons)
							} else {
								stringResource(R.string.jellyseerr_select_all_available)
							},
						checked = selectAllChecked,
						enabled = true,
						accentColor = accentColor,
						onCheckedChange = { checked ->
							selectAllChecked = checked
							availableSeasons.forEach { season ->
								checkedStates[season - 1] = checked
							}
						},
					)
				}

				// Divider
				Box(
					modifier =
						Modifier
							.fillMaxWidth()
							.height(1.dp)
							.background(Color.White.copy(alpha = 0.10f)),
				)

				Spacer(modifier = Modifier.height(12.dp))

				// Season list
				LazyColumn(
					modifier = Modifier.height(280.dp),
				) {
					itemsIndexed((1..numberOfSeasons).toList()) { _, season ->
						val isUnavailable = season in unavailableSeasons
						SeasonCheckboxRow(
							label =
								if (isUnavailable) {
									stringResource(R.string.jellyseerr_season_already_requested, season)
								} else {
									stringResource(R.string.jellyseerr_season_number, season)
								},
							checked = checkedStates[season - 1],
							enabled = !isUnavailable,
							accentColor = accentColor,
							onCheckedChange = { checked ->
								checkedStates[season - 1] = checked
								updateSelectAll()
							},
						)
					}
				}

				Spacer(modifier = Modifier.height(16.dp))

				// Buttons row
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Center,
				) {
					// Cancel
					VegafoXButton(
						text = stringResource(R.string.btn_cancel),
						variant = VegafoXButtonVariant.Ghost,
						compact = true,
						onClick = onDismiss,
					)

					Spacer(modifier = Modifier.width(24.dp))

					// Confirm
					VegafoXButton(
						text = stringResource(R.string.btn_request_selected),
						variant = VegafoXButtonVariant.Primary,
						compact = true,
						autoFocus = true,
						onClick = {
							val selected =
								checkedStates
									.mapIndexedNotNull { index, checked ->
										if (checked) index + 1 else null
									}
							if (selected.isNotEmpty()) {
								onConfirm(selected)
								onDismiss()
							}
						},
					)
				}
			}
		}
	}
}

@Composable
private fun SeasonCheckboxRow(
	label: String,
	checked: Boolean,
	enabled: Boolean,
	accentColor: Color,
	onCheckedChange: (Boolean) -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	Row(
		modifier =
			Modifier
				.fillMaxWidth()
				.then(
					if (enabled) {
						Modifier
							.clickable(interactionSource = interactionSource, indication = null) {
								onCheckedChange(!checked)
							}.focusable(interactionSource = interactionSource)
					} else {
						Modifier
					},
				).background(if (isFocused) Color.White.copy(alpha = 0.05f) else Color.Transparent)
				.then(
					if (isFocused) {
						Modifier.border(
							width = 3.dp,
							color = VegafoXColors.OrangePrimary,
							shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 4.dp, bottomEnd = 4.dp),
						)
					} else {
						Modifier
					},
				).padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Checkbox(
			checked = checked,
			onCheckedChange = if (enabled) onCheckedChange else null,
			enabled = enabled,
			colors =
				CheckboxDefaults.colors(
					checkedColor = accentColor,
					uncheckedColor = VegafoXColors.TextSecondary,
					disabledCheckedColor = VegafoXColors.TextDisabled,
					disabledUncheckedColor = VegafoXColors.TextDisabled,
				),
		)

		Spacer(modifier = Modifier.width(8.dp))

		Text(
			text = label,
			style =
				TextStyle(
					fontSize = 16.sp,
					color = if (enabled) VegafoXColors.TextPrimary else VegafoXColors.TextDisabled,
				),
		)
	}
}

@Composable
internal fun DialogActionButton(
	text: String,
	backgroundColor: Color,
	focusedBackgroundColor: Color,
	accentColor: Color,
	focusRequester: FocusRequester? = null,
	onClick: () -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val focusModifier =
		if (focusRequester != null) {
			Modifier.focusRequester(focusRequester)
		} else {
			Modifier
		}

	val buttonShape = RoundedCornerShape(14.dp)
	Box(
		modifier =
			focusModifier
				.clip(buttonShape)
				.background(if (isFocused) focusedBackgroundColor else backgroundColor)
				.then(
					if (isFocused) {
						Modifier.border(2.dp, accentColor, buttonShape)
					} else {
						Modifier
					},
				).clickable(interactionSource = interactionSource, indication = null) { onClick() }
				.focusable(interactionSource = interactionSource)
				.padding(horizontal = 24.dp, vertical = 12.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = text,
			style =
				TextStyle(
					fontSize = 14.sp,
					color = VegafoXColors.TextPrimary,
				),
		)
	}
}
