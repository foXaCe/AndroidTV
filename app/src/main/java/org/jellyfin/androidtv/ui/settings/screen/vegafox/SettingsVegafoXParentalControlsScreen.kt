package org.jellyfin.androidtv.ui.settings.screen.vegafox

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.ParentalControlsRepository
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.form.Checkbox
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.koin.compose.koinInject

@Composable
fun SettingsVegafoXParentalControlsScreen() {
	val parentalControlsRepository = koinInject<ParentalControlsRepository>()
	
	var availableRatings by remember { mutableStateOf<List<String>>(emptyList()) }
	var isLoading by remember { mutableStateOf(true) }
	val blockedRatings = remember { mutableStateListOf<String>() }

	LaunchedEffect(Unit) {
		availableRatings = parentalControlsRepository.getAvailableRatings()
		blockedRatings.clear()
		blockedRatings.addAll(parentalControlsRepository.getBlockedRatings())
		isLoading = false
	}

	SettingsColumn {
		item {
			Text(
				text = stringResource(R.string.pref_parental_controls),
				fontFamily = BebasNeue,
				fontSize = 22.sp,
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
			)
		}

		item { ListSection(headingContent = { Text(stringResource(R.string.pref_parental_controls_category_ratings)) }) }

		if (isLoading) {
			item {
				ListButton(
					headingContent = { Text(stringResource(R.string.pref_parental_controls_loading)) },
					enabled = false,
					onClick = { },
				)
			}
		} else if (availableRatings.isEmpty()) {
			item {
				ListButton(
					headingContent = { Text(stringResource(R.string.lbl_no_ratings_available)) },
					enabled = false,
					onClick = { },
				)
			}
		} else {
			items(availableRatings) { rating ->
				val isBlocked = blockedRatings.contains(rating)
				ListButton(
					headingContent = { Text(rating) },
					captionContent = {
						Text(
							if (isBlocked) {
								stringResource(R.string.pref_parental_controls_rating_blocked)
							} else {
								stringResource(R.string.pref_parental_controls_rating_allowed)
							},
						)
					},
					trailingContent = { Checkbox(checked = isBlocked) },
					onClick = {
						if (isBlocked) {
							blockedRatings.remove(rating)
						} else {
							blockedRatings.add(rating)
						}
						parentalControlsRepository.setBlockedRatings(blockedRatings.toSet())
					},
				)
			}
		}

		item { ListSection(headingContent = { Text(stringResource(R.string.pref_parental_controls_category_info)) }) }

		item {
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_parental_controls_info_description)) },
				enabled = false,
				onClick = { },
			)
		}
	}
}
