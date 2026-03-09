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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.focusBorderColor

@Composable
fun SeasonSelectionDialog(
	showName: String,
	numberOfSeasons: Int,
	is4k: Boolean,
	unavailableSeasons: Set<Int>,
	onConfirm: (selectedSeasons: List<Int>) -> Unit,
	onDismiss: () -> Unit,
) {
	val accentColor = focusBorderColor()
	val confirmFocusRequester = remember { FocusRequester() }

	val checkedStates = remember {
		mutableStateListOf<Boolean>().apply {
			for (season in 1..numberOfSeasons) {
				add(season !in unavailableSeasons)
			}
		}
	}

	val availableSeasons = remember(numberOfSeasons, unavailableSeasons) {
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
				modifier = Modifier
					.widthIn(min = 400.dp, max = 600.dp)
					.clip(JellyfinTheme.shapes.dialog)
					.background(JellyfinTheme.colorScheme.surface)
					.border(1.dp, JellyfinTheme.colorScheme.outlineVariant, JellyfinTheme.shapes.dialog)
					.padding(horizontal = 24.dp, vertical = 24.dp),
			) {
				// Title
				Text(
					text = stringResource(R.string.jellyseerr_select_seasons),
					style = JellyfinTheme.typography.titleLarge,
					color = JellyfinTheme.colorScheme.textPrimary,
					modifier = Modifier.padding(bottom = 6.dp),
				)

				// Subtitle
				Text(
					text = "$showName ${if (is4k) "(4K)" else "(HD)"}",
					style = JellyfinTheme.typography.bodyMedium,
					color = JellyfinTheme.colorScheme.textSecondary,
					modifier = Modifier.padding(bottom = 16.dp),
				)

				// Select All checkbox
				if (availableSeasons.isNotEmpty()) {
					SeasonCheckboxRow(
						label = if (unavailableSeasons.isEmpty()) {
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
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(JellyfinTheme.colorScheme.outlineVariant),
				)

				Spacer(modifier = Modifier.height(12.dp))

				// Season list
				LazyColumn(
					modifier = Modifier.height(280.dp),
				) {
					itemsIndexed((1..numberOfSeasons).toList()) { _, season ->
						val isUnavailable = season in unavailableSeasons
						SeasonCheckboxRow(
							label = if (isUnavailable) {
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
					DialogActionButton(
						text = stringResource(R.string.btn_cancel),
						backgroundColor = JellyfinTheme.colorScheme.surfaceContainer,
						focusedBackgroundColor = JellyfinTheme.colorScheme.textSecondary,
						accentColor = accentColor,
						onClick = onDismiss,
					)

					Spacer(modifier = Modifier.width(24.dp))

					// Confirm
					DialogActionButton(
						text = stringResource(R.string.btn_request_selected),
						backgroundColor = JellyfinTheme.colorScheme.info,
						focusedBackgroundColor = JellyfinTheme.colorScheme.primary,
						accentColor = accentColor,
						focusRequester = confirmFocusRequester,
						onClick = {
							val selected = checkedStates
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

		LaunchedEffect(Unit) {
			confirmFocusRequester.requestFocus()
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
		modifier = Modifier
			.fillMaxWidth()
			.then(
				if (enabled) Modifier
					.clickable(interactionSource = interactionSource, indication = null) {
						onCheckedChange(!checked)
					}
					.focusable(interactionSource = interactionSource)
				else Modifier
			)
			.background(if (isFocused) JellyfinTheme.colorScheme.surfaceBright else Color.Transparent)
			.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Checkbox(
			checked = checked,
			onCheckedChange = if (enabled) onCheckedChange else null,
			enabled = enabled,
			colors = CheckboxDefaults.colors(
				checkedColor = accentColor,
				uncheckedColor = JellyfinTheme.colorScheme.textSecondary,
				disabledCheckedColor = JellyfinTheme.colorScheme.textDisabled,
				disabledUncheckedColor = JellyfinTheme.colorScheme.textDisabled,
			),
		)

		Spacer(modifier = Modifier.width(8.dp))

		Text(
			text = label,
			style = JellyfinTheme.typography.bodyMedium,
			color = if (enabled) JellyfinTheme.colorScheme.textPrimary else JellyfinTheme.colorScheme.textDisabled,
			modifier = if (!enabled) Modifier.padding() else Modifier,
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

	val focusModifier = if (focusRequester != null) {
		Modifier.focusRequester(focusRequester)
	} else {
		Modifier
	}

	Box(
		modifier = focusModifier
			.clip(JellyfinTheme.shapes.small)
			.background(if (isFocused) focusedBackgroundColor else backgroundColor)
			.then(
				if (isFocused) Modifier.border(2.dp, accentColor, JellyfinTheme.shapes.small)
				else Modifier
			)
			.clickable(interactionSource = interactionSource, indication = null) { onClick() }
			.focusable(interactionSource = interactionSource)
			.padding(horizontal = 24.dp, vertical = 12.dp),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = text,
			style = JellyfinTheme.typography.labelLarge,
			color = JellyfinTheme.colorScheme.textPrimary,
		)
	}
}
