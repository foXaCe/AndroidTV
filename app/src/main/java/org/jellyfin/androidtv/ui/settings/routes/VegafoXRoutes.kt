package org.jellyfin.androidtv.ui.settings.routes

import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.navigation.RouteComposable
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.composable.OptionListScreen
import org.jellyfin.androidtv.ui.settings.screen.vegafox.SettingsJellyseerrRowsScreen
import org.jellyfin.androidtv.ui.settings.screen.vegafox.SettingsJellyseerrScreen
import org.jellyfin.androidtv.ui.settings.screen.vegafox.SettingsPluginScreen
import org.jellyfin.androidtv.ui.settings.screen.vegafox.SettingsVegafoXParentalControlsScreen
import org.jellyfin.androidtv.ui.settings.screen.vegafox.SettingsVegafoXSyncPlayScreen
import org.jellyfin.androidtv.ui.settings.screen.vegafox.browsingBlurEntry
import org.jellyfin.androidtv.ui.settings.screen.vegafox.detailsBlurEntry
import org.jellyfin.androidtv.ui.settings.screen.vegafox.mediaBarColorEntry
import org.jellyfin.androidtv.ui.settings.screen.vegafox.mediaBarContentTypeEntry
import org.jellyfin.androidtv.ui.settings.screen.vegafox.mediaBarItemCountEntry
import org.jellyfin.androidtv.ui.settings.screen.vegafox.mediaBarOpacityEntry
import org.jellyfin.androidtv.ui.settings.screen.vegafox.navbarPositionEntry
import org.jellyfin.androidtv.ui.settings.screen.vegafox.shuffleContentTypeEntry
import org.jellyfin.androidtv.ui.settings.screen.vegafox.themeMusicVolumeEntry
import org.koin.compose.koinInject

val vegafoXRoutes =
	mapOf<String, RouteComposable>(
		Routes.JELLYSEERR to {
			ScreenIdOverlay(ScreenIds.SETTINGS_JELLYSEERR_ID, ScreenIds.SETTINGS_JELLYSEERR_NAME) {
				SettingsJellyseerrScreen()
			}
		},
		Routes.JELLYSEERR_ROWS to {
			ScreenIdOverlay(ScreenIds.SETTINGS_JELLYSEERR_ROWS_ID, ScreenIds.SETTINGS_JELLYSEERR_ROWS_NAME) {
				SettingsJellyseerrRowsScreen()
			}
		},
		Routes.PLUGIN to {
			ScreenIdOverlay(ScreenIds.SETTINGS_PLUGIN_ID, ScreenIds.SETTINGS_PLUGIN_NAME) {
				SettingsPluginScreen()
			}
		},
		Routes.VEGAFOX_NAVBAR_POSITION to {
			OptionListScreen(navbarPositionEntry, koinInject<UserPreferences>())
		},
		Routes.VEGAFOX_SHUFFLE_CONTENT_TYPE to {
			OptionListScreen(shuffleContentTypeEntry, koinInject<UserPreferences>())
		},
		Routes.VEGAFOX_MEDIA_BAR_CONTENT_TYPE to {
			OptionListScreen(mediaBarContentTypeEntry, koinInject<UserSettingPreferences>())
		},
		Routes.VEGAFOX_MEDIA_BAR_ITEM_COUNT to {
			OptionListScreen(mediaBarItemCountEntry, koinInject<UserSettingPreferences>())
		},
		Routes.VEGAFOX_MEDIA_BAR_OPACITY to {
			OptionListScreen(mediaBarOpacityEntry, koinInject<UserSettingPreferences>())
		},
		Routes.VEGAFOX_MEDIA_BAR_COLOR to {
			OptionListScreen(mediaBarColorEntry, koinInject<UserSettingPreferences>())
		},
		Routes.VEGAFOX_THEME_MUSIC_VOLUME to {
			OptionListScreen(themeMusicVolumeEntry, koinInject<UserSettingPreferences>())
		},
		Routes.VEGAFOX_DETAILS_BLUR to {
			OptionListScreen(detailsBlurEntry, koinInject<UserSettingPreferences>())
		},
		Routes.VEGAFOX_BROWSING_BLUR to {
			OptionListScreen(browsingBlurEntry, koinInject<UserSettingPreferences>())
		},
		Routes.VEGAFOX_PARENTAL_CONTROLS to {
			ScreenIdOverlay(ScreenIds.SETTINGS_PARENTAL_ID, ScreenIds.SETTINGS_PARENTAL_NAME) {
				SettingsVegafoXParentalControlsScreen()
			}
		},
		Routes.VEGAFOX_SYNCPLAY to {
			ScreenIdOverlay(ScreenIds.SETTINGS_SYNCPLAY_ID, ScreenIds.SETTINGS_SYNCPLAY_NAME) {
				SettingsVegafoXSyncPlayScreen()
			}
		},
	)
