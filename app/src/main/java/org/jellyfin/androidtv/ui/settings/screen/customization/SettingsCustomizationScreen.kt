package org.jellyfin.androidtv.ui.settings.screen.customization

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.auth.repository.UserRepository
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
import org.jellyfin.androidtv.ui.settings.screen.vegafox.getShuffleContentTypeLabel
import org.koin.compose.koinInject

@Composable
fun SettingsCustomizationScreen(
	userPreferences: UserPreferences = koinInject(),
	userRepository: UserRepository = koinInject(),
) {
	val router = LocalRouter.current
	val context = LocalContext.current
	val userId =
		userRepository.currentUser
			.collectAsState()
			.value
			?.id
	val userSettingPreferences = remember(userId) { UserSettingPreferences(context, userId) }

	SettingsColumn {
		item {
			Text(
				text = stringResource(R.string.pref_customization),
				fontFamily = BebasNeue,
				fontSize = 22.sp,
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
			)
		}

		// ── Browsing ──

		item { ListSection(headingContent = { Text(stringResource(R.string.pref_browsing)) }) }

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.GridView), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_libraries)) },
				onClick = { router.push(Routes.LIBRARIES) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Home), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.home_prefs)) },
				onClick = { router.push(Routes.HOME) },
			)
		}

		item {
			var focusColor by rememberPreference(userSettingPreferences, UserSettingPreferences.focusColor)

			ListButton(
				headingContent = { Text(stringResource(R.string.pref_focus_color)) },
				captionContent = { Text(stringResource(focusColor.nameRes)) },
				onClick = { router.push(Routes.CUSTOMIZATION_THEME) },
			)
		}

		item {
			var clockBehavior by rememberPreference(userPreferences, UserPreferences.clockBehavior)

			ListButton(
				headingContent = { Text(stringResource(R.string.pref_clock_display)) },
				captionContent = { Text(stringResource(clockBehavior.nameRes)) },
				onClick = { router.push(Routes.CUSTOMIZATION_CLOCK) },
			)
		}

		item {
			var watchedIndicatorBehavior by rememberPreference(userPreferences, UserPreferences.watchedIndicatorBehavior)

			ListButton(
				headingContent = { Text(stringResource(R.string.pref_watched_indicator)) },
				captionContent = { Text(stringResource(watchedIndicatorBehavior.nameRes)) },
				onClick = { router.push(Routes.CUSTOMIZATION_WATCHED_INDICATOR) },
			)
		}

		item {
			var backdropEnabled by rememberPreference(userPreferences, UserPreferences.backdropEnabled)

			ListButton(
				headingContent = { Text(stringResource(R.string.lbl_show_backdrop)) },
				trailingContent = { Checkbox(checked = backdropEnabled) },
				captionContent = { Text(stringResource(R.string.pref_show_backdrop_description)) },
				onClick = { backdropEnabled = !backdropEnabled },
			)
		}

		item {
			var seriesThumbnailsEnabled by rememberPreference(userPreferences, UserPreferences.seriesThumbnailsEnabled)

			ListButton(
				headingContent = { Text(stringResource(R.string.lbl_use_series_thumbnails)) },
				trailingContent = { Checkbox(checked = seriesThumbnailsEnabled) },
				captionContent = { Text(stringResource(R.string.lbl_use_series_thumbnails_description)) },
				onClick = { seriesThumbnailsEnabled = !seriesThumbnailsEnabled },
			)
		}

		// ── Toolbar ──

		item { ListSection(headingContent = { Text(stringResource(R.string.pref_toolbar_customization)) }) }

		item {
			val navbarPosition by rememberPreference(userPreferences, UserPreferences.navbarPosition)
			val navbarLabel =
				when (navbarPosition) {
					org.jellyfin.androidtv.preference.constant.NavbarPosition.TOP -> stringResource(R.string.pref_navbar_position_top)
					org.jellyfin.androidtv.preference.constant.NavbarPosition.LEFT -> stringResource(R.string.pref_navbar_position_left)
				}
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_navbar_position)) },
				captionContent = { Text(navbarLabel) },
				onClick = { router.push(Routes.VEGAFOX_NAVBAR_POSITION) },
			)
		}

		item {
			var showShuffleButton by rememberPreference(userPreferences, UserPreferences.showShuffleButton)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_show_shuffle_button)) },
				captionContent = { Text(stringResource(R.string.pref_show_shuffle_button_description)) },
				trailingContent = { Checkbox(checked = showShuffleButton) },
				onClick = { showShuffleButton = !showShuffleButton },
			)
		}

		item {
			var showGenresButton by rememberPreference(userPreferences, UserPreferences.showGenresButton)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_show_genres_button)) },
				captionContent = { Text(stringResource(R.string.pref_show_genres_button_description)) },
				trailingContent = { Checkbox(checked = showGenresButton) },
				onClick = { showGenresButton = !showGenresButton },
			)
		}

		item {
			var showFavoritesButton by rememberPreference(userPreferences, UserPreferences.showFavoritesButton)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_show_favorites_button)) },
				captionContent = { Text(stringResource(R.string.pref_show_favorites_button_description)) },
				trailingContent = { Checkbox(checked = showFavoritesButton) },
				onClick = { showFavoritesButton = !showFavoritesButton },
			)
		}

		item {
			var showLibrariesInToolbar by rememberPreference(userPreferences, UserPreferences.showLibrariesInToolbar)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_show_libraries_in_toolbar)) },
				captionContent = { Text(stringResource(R.string.pref_show_libraries_in_toolbar_description)) },
				trailingContent = { Checkbox(checked = showLibrariesInToolbar) },
				onClick = { showLibrariesInToolbar = !showLibrariesInToolbar },
			)
		}

		item {
			val shuffleContentType by rememberPreference(userPreferences, UserPreferences.shuffleContentType)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_shuffle_content_type)) },
				captionContent = { Text(getShuffleContentTypeLabel(shuffleContentType)) },
				onClick = { router.push(Routes.VEGAFOX_SHUFFLE_CONTENT_TYPE) },
			)
		}

		// ── Home Behavior ──

		item { ListSection(headingContent = { Text(stringResource(R.string.home_section_settings)) }) }

		item {
			var enableMultiServerLibraries by rememberPreference(userPreferences, UserPreferences.enableMultiServerLibraries)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_multi_server_libraries)) },
				captionContent = { Text(stringResource(R.string.pref_multi_server_libraries_description)) },
				trailingContent = { Checkbox(checked = enableMultiServerLibraries) },
				onClick = { enableMultiServerLibraries = !enableMultiServerLibraries },
			)
		}

		item {
			var enableFolderView by rememberPreference(userPreferences, UserPreferences.enableFolderView)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_enable_folder_view)) },
				captionContent = { Text(stringResource(R.string.pref_enable_folder_view_description)) },
				trailingContent = { Checkbox(checked = enableFolderView) },
				onClick = { enableFolderView = !enableFolderView },
			)
		}

		item {
			var confirmExit by rememberPreference(userPreferences, UserPreferences.confirmExit)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_confirm_exit)) },
				captionContent = { Text(stringResource(R.string.pref_confirm_exit_description)) },
				trailingContent = { Checkbox(checked = confirmExit) },
				onClick = { confirmExit = !confirmExit },
			)
		}

		// ── Appearance (blur settings removed — hardcoded values) ──
	}
}
