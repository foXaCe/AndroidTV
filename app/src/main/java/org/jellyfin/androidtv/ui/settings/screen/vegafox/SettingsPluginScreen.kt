package org.jellyfin.androidtv.ui.settings.screen.vegafox

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.pluginsync.PluginSyncService
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.form.Checkbox
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.compat.rememberPreference
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.koin.compose.koinInject

@Composable
fun SettingsPluginScreen() {
	val router = LocalRouter.current
	val coroutineScope = rememberCoroutineScope()
	val userPreferences = koinInject<UserPreferences>()
	val userSettingPreferences = koinInject<UserSettingPreferences>()
	val pluginSyncService = koinInject<PluginSyncService>()

	SettingsColumn {
		item {
			Text(
				text = stringResource(R.string.pref_plugin_settings),
				fontFamily = BebasNeue,
				fontSize = 22.sp,
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
			)
		}

		// ── Plugin Sync ──

		item {
			var pluginSyncEnabled by rememberPreference(userPreferences, UserPreferences.pluginSyncEnabled)
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_vegafox_fox), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_plugin_sync_enable)) },
				captionContent = { Text(stringResource(R.string.pref_plugin_sync_description)) },
				trailingContent = { Checkbox(checked = pluginSyncEnabled) },
				onClick = {
					pluginSyncEnabled = !pluginSyncEnabled
					if (pluginSyncEnabled) {
						coroutineScope.launch {
							pluginSyncService.initialSync()
							pluginSyncService.configureJellyseerrProxy()
						}
					} else {
						pluginSyncService.unregisterChangeListeners()
					}
				},
			)
		}

		// ── Media Bar ──

		item { ListSection(headingContent = { Text(stringResource(R.string.pref_media_bar_title)) }) }

		item {
			var mediaBarEnabled by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarEnabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_media_bar_enable)) },
				captionContent = { Text(stringResource(R.string.pref_media_bar_enable_summary)) },
				trailingContent = { Checkbox(checked = mediaBarEnabled) },
				onClick = { mediaBarEnabled = !mediaBarEnabled },
			)
		}

		item {
			val mediaBarEnabled by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarEnabled)
			val mediaBarContentType by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarContentType)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_media_bar_content_type)) },
				captionContent = { Text(getShuffleContentTypeLabel(mediaBarContentType)) },
				enabled = mediaBarEnabled,
				onClick = { router.push(Routes.VEGAFOX_MEDIA_BAR_CONTENT_TYPE) },
			)
		}

		item {
			val mediaBarEnabled by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarEnabled)
			val mediaBarItemCount by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarItemCount)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_media_bar_item_count)) },
				captionContent = { Text(getMediaBarItemCountLabel(mediaBarItemCount)) },
				enabled = mediaBarEnabled,
				onClick = { router.push(Routes.VEGAFOX_MEDIA_BAR_ITEM_COUNT) },
			)
		}

		item {
			val mediaBarEnabled by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarEnabled)
			val mediaBarOverlayOpacity by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarOverlayOpacity)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_media_bar_overlay_opacity)) },
				captionContent = { Text("$mediaBarOverlayOpacity%") },
				enabled = mediaBarEnabled,
				onClick = { router.push(Routes.VEGAFOX_MEDIA_BAR_OPACITY) },
			)
		}

		item {
			val mediaBarEnabled by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarEnabled)
			val mediaBarOverlayColor by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarOverlayColor)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_media_bar_overlay_color)) },
				captionContent = { Text(getOverlayColorLabel(mediaBarOverlayColor)) },
				enabled = mediaBarEnabled,
				onClick = { router.push(Routes.VEGAFOX_MEDIA_BAR_COLOR) },
			)
		}

		item {
			val mediaBarEnabled by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarEnabled)
			var trailerPreview by rememberPreference(userSettingPreferences, UserSettingPreferences.mediaBarTrailerPreview)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_media_bar_trailer_preview)) },
				captionContent = { Text(stringResource(R.string.pref_media_bar_trailer_preview_summary)) },
				trailingContent = { Checkbox(checked = trailerPreview) },
				enabled = mediaBarEnabled,
				onClick = { trailerPreview = !trailerPreview },
			)
		}

		item {
			var episodePreview by rememberPreference(userSettingPreferences, UserSettingPreferences.episodePreviewEnabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_episode_preview)) },
				captionContent = { Text(stringResource(R.string.pref_episode_preview_summary)) },
				trailingContent = { Checkbox(checked = episodePreview) },
				onClick = { episodePreview = !episodePreview },
			)
		}

		item {
			var previewAudio by rememberPreference(userSettingPreferences, UserSettingPreferences.previewAudioEnabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_preview_audio)) },
				captionContent = { Text(stringResource(R.string.pref_preview_audio_summary)) },
				trailingContent = { Checkbox(checked = previewAudio) },
				onClick = { previewAudio = !previewAudio },
			)
		}

		// ── Theme Music ──

		item { ListSection(headingContent = { Text(stringResource(R.string.pref_theme_music_title)) }) }

		item {
			var themeMusicEnabled by rememberPreference(userSettingPreferences, UserSettingPreferences.themeMusicEnabled)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_theme_music_enable)) },
				captionContent = { Text(stringResource(R.string.pref_theme_music_enable_summary)) },
				trailingContent = { Checkbox(checked = themeMusicEnabled) },
				onClick = { themeMusicEnabled = !themeMusicEnabled },
			)
		}

		item {
			val themeMusicEnabled by rememberPreference(userSettingPreferences, UserSettingPreferences.themeMusicEnabled)
			var themeMusicOnHomeRows by rememberPreference(userSettingPreferences, UserSettingPreferences.themeMusicOnHomeRows)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_theme_music_on_home_rows)) },
				captionContent = { Text(stringResource(R.string.pref_theme_music_on_home_rows_summary)) },
				trailingContent = { Checkbox(checked = themeMusicOnHomeRows) },
				enabled = themeMusicEnabled,
				onClick = { themeMusicOnHomeRows = !themeMusicOnHomeRows },
			)
		}

		item {
			val themeMusicEnabled by rememberPreference(userSettingPreferences, UserSettingPreferences.themeMusicEnabled)
			val themeMusicVolume by rememberPreference(userSettingPreferences, UserSettingPreferences.themeMusicVolume)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_theme_music_volume)) },
				captionContent = { Text("$themeMusicVolume%") },
				enabled = themeMusicEnabled,
				onClick = { router.push(Routes.VEGAFOX_THEME_MUSIC_VOLUME) },
			)
		}

		// ── Ratings ──

		item { ListSection(headingContent = { Text(stringResource(R.string.pref_enable_additional_ratings)) }) }

		item {
			var enableAdditionalRatings by rememberPreference(userSettingPreferences, UserSettingPreferences.enableAdditionalRatings)
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Star), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_enable_additional_ratings)) },
				captionContent = { Text(stringResource(R.string.pref_enable_additional_ratings_description)) },
				trailingContent = { Checkbox(checked = enableAdditionalRatings) },
				onClick = { enableAdditionalRatings = !enableAdditionalRatings },
			)
		}

		item {
			var showRatingLabels by rememberPreference(userSettingPreferences, UserSettingPreferences.showRatingLabels)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_show_rating_labels)) },
				captionContent = { Text(stringResource(R.string.pref_show_rating_labels_description)) },
				trailingContent = { Checkbox(checked = showRatingLabels) },
				onClick = { showRatingLabels = !showRatingLabels },
			)
		}

		item { ListSection(headingContent = { Text(stringResource(R.string.pref_episode_ratings)) }) }

		item {
			var enableEpisodeRatings by rememberPreference(userSettingPreferences, UserSettingPreferences.enableEpisodeRatings)
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Star), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_episode_ratings)) },
				captionContent = { Text(stringResource(R.string.pref_episode_ratings_description)) },
				trailingContent = { Checkbox(checked = enableEpisodeRatings) },
				onClick = { enableEpisodeRatings = !enableEpisodeRatings },
			)
		}

		// ── Jellyseerr ──

		item { ListSection(headingContent = { Text(stringResource(R.string.jellyseerr)) }) }

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_jellyseerr_jellyfish), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.jellyseerr_settings)) },
				captionContent = { Text(stringResource(R.string.jellyseerr_settings_description)) },
				onClick = { router.push(Routes.JELLYSEERR) },
			)
		}

		// ── Parental Controls ──

		item { ListSection(headingContent = { Text(stringResource(R.string.pref_parental_controls)) }) }

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Lock), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_parental_controls)) },
				captionContent = { Text(stringResource(R.string.pref_parental_controls_description)) },
				onClick = { router.push(Routes.VEGAFOX_PARENTAL_CONTROLS) },
			)
		}
	}
}
