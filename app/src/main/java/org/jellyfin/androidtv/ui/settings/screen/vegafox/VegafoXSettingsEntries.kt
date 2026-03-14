package org.jellyfin.androidtv.ui.settings.screen.vegafox

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.preference.constant.NavbarPosition
import org.jellyfin.androidtv.ui.settings.composable.SettingsEntry

val navbarPositionEntry =
	SettingsEntry(
		preference = UserPreferences.navbarPosition,
		titleRes = R.string.pref_navbar_position,
		options = NavbarPosition.entries,
		label = @Composable { option ->
			stringResource(
				when (option) {
					NavbarPosition.TOP -> R.string.pref_navbar_position_top
					NavbarPosition.LEFT -> R.string.pref_navbar_position_left
				},
			)
		},
		overlineRes = R.string.vegafox_settings,
		descriptionRes = R.string.pref_navbar_position_description,
	)

val shuffleContentTypeEntry =
	SettingsEntry(
		preference = UserPreferences.shuffleContentType,
		titleRes = R.string.pref_shuffle_content_type,
		options = listOf("movies", "tv", "both"),
		label = @Composable { option ->
			stringResource(
				when (option) {
					"movies" -> R.string.pref_shuffle_movies
					"tv" -> R.string.pref_shuffle_tv
					else -> R.string.pref_shuffle_both
				},
			)
		},
		overlineRes = R.string.vegafox_settings,
	)

val mediaBarContentTypeEntry =
	SettingsEntry(
		preference = UserSettingPreferences.mediaBarContentType,
		titleRes = R.string.pref_media_bar_content_type,
		options = listOf("movies", "tv", "both"),
		label = @Composable { option ->
			stringResource(
				when (option) {
					"movies" -> R.string.pref_shuffle_movies
					"tv" -> R.string.pref_shuffle_tv
					else -> R.string.pref_shuffle_both
				},
			)
		},
		overlineRes = R.string.pref_media_bar_title,
	)

val mediaBarItemCountEntry =
	SettingsEntry(
		preference = UserSettingPreferences.mediaBarItemCount,
		titleRes = R.string.pref_media_bar_item_count,
		options = listOf("5", "10", "15"),
		label = @Composable { option ->
			stringResource(
				when (option) {
					"5" -> R.string.pref_media_bar_5_items
					"10" -> R.string.pref_media_bar_10_items
					else -> R.string.pref_media_bar_15_items
				},
			)
		},
		overlineRes = R.string.pref_media_bar_title,
	)

val mediaBarOpacityEntry =
	SettingsEntry(
		preference = UserSettingPreferences.mediaBarOverlayOpacity,
		titleRes = R.string.pref_media_bar_overlay_opacity,
		options = (10..90 step 5).toList(),
		label = @Composable { "$it%" },
		overlineRes = R.string.pref_media_bar_title,
		descriptionRes = R.string.pref_media_bar_overlay_opacity_summary,
	)

val mediaBarColorEntry =
	SettingsEntry(
		preference = UserSettingPreferences.mediaBarOverlayColor,
		titleRes = R.string.pref_media_bar_overlay_color,
		options =
			listOf(
				"black",
				"gray",
				"dark_blue",
				"purple",
				"teal",
				"navy",
				"charcoal",
				"brown",
				"dark_red",
				"dark_green",
				"slate",
				"indigo",
			),
		label = @Composable { option ->
			stringResource(
				when (option) {
					"black" -> R.string.pref_media_bar_color_black
					"gray" -> R.string.pref_media_bar_color_gray
					"dark_blue" -> R.string.pref_media_bar_color_dark_blue
					"purple" -> R.string.pref_media_bar_color_purple
					"teal" -> R.string.pref_media_bar_color_teal
					"navy" -> R.string.pref_media_bar_color_navy
					"charcoal" -> R.string.pref_media_bar_color_charcoal
					"brown" -> R.string.pref_media_bar_color_brown
					"dark_red" -> R.string.pref_media_bar_color_dark_red
					"dark_green" -> R.string.pref_media_bar_color_dark_green
					"slate" -> R.string.pref_media_bar_color_slate
					"indigo" -> R.string.pref_media_bar_color_indigo
					else -> R.string.pref_media_bar_color_black
				},
			)
		},
		overlineRes = R.string.pref_media_bar_title,
	)

val browsingBlurEntry =
	SettingsEntry(
		preference = UserSettingPreferences.browsingBackgroundBlurAmount,
		titleRes = R.string.pref_browsing_background_blur_amount,
		options = listOf(0, 5, 10, 15, 20),
		label = @Composable { option ->
			stringResource(
				when (option) {
					0 -> R.string.pref_blur_none
					5 -> R.string.pref_blur_light
					10 -> R.string.pref_blur_medium
					15 -> R.string.pref_blur_strong
					else -> R.string.pref_blur_extra_strong
				},
			)
		},
		overlineRes = R.string.pref_appearance,
		descriptionRes = R.string.pref_browsing_background_blur_amount_description,
	)

val detailsBlurEntry =
	SettingsEntry(
		preference = UserSettingPreferences.detailsBackgroundBlurAmount,
		titleRes = R.string.pref_details_background_blur_amount,
		options = listOf(0, 5, 10, 15, 20),
		label = @Composable { option ->
			stringResource(
				when (option) {
					0 -> R.string.pref_blur_none
					5 -> R.string.pref_blur_light
					10 -> R.string.pref_blur_medium
					15 -> R.string.pref_blur_strong
					else -> R.string.pref_blur_extra_strong
				},
			)
		},
		overlineRes = R.string.pref_appearance,
		descriptionRes = R.string.pref_details_background_blur_amount_description,
	)

val seasonalSurpriseEntry =
	SettingsEntry(
		preference = UserPreferences.seasonalSurprise,
		titleRes = R.string.pref_seasonal_surprise,
		options = listOf("auto", "none", "winter", "spring", "summer", "halloween", "fall"),
		label = @Composable { option ->
			stringResource(
				when (option) {
					"auto" -> R.string.pref_seasonal_auto
					"none" -> R.string.pref_seasonal_none
					"winter" -> R.string.pref_seasonal_winter
					"spring" -> R.string.pref_seasonal_spring
					"summer" -> R.string.pref_seasonal_summer
					"halloween" -> R.string.pref_seasonal_halloween
					else -> R.string.pref_seasonal_fall
				},
			)
		},
		overlineRes = R.string.pref_appearance,
	)

val themeMusicVolumeEntry =
	SettingsEntry(
		preference = UserSettingPreferences.themeMusicVolume,
		titleRes = R.string.pref_theme_music_volume,
		options = (10..100 step 5).toList(),
		label = @Composable { "$it%" },
		overlineRes = R.string.pref_theme_music_title,
		descriptionRes = R.string.pref_theme_music_volume_summary,
	)
