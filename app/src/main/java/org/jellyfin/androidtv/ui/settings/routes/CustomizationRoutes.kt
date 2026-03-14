package org.jellyfin.androidtv.ui.settings.routes

import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.navigation.RouteComposable
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.screen.customization.SettingsCustomizationClockScreen
import org.jellyfin.androidtv.ui.settings.screen.customization.SettingsCustomizationScreen
import org.jellyfin.androidtv.ui.settings.screen.customization.SettingsCustomizationThemeScreen
import org.jellyfin.androidtv.ui.settings.screen.customization.SettingsCustomizationWatchedIndicatorScreen
import org.jellyfin.androidtv.ui.settings.screen.customization.subtitle.SettingsSubtitleTextStrokeColorScreen
import org.jellyfin.androidtv.ui.settings.screen.customization.subtitle.SettingsSubtitlesBackgroundColorScreen
import org.jellyfin.androidtv.ui.settings.screen.customization.subtitle.SettingsSubtitlesScreen
import org.jellyfin.androidtv.ui.settings.screen.customization.subtitle.SettingsSubtitlesTextColorScreen
import org.jellyfin.androidtv.ui.settings.screen.screensaver.SettingsScreensaverAgeRatingScreen
import org.jellyfin.androidtv.ui.settings.screen.screensaver.SettingsScreensaverDimmingScreen
import org.jellyfin.androidtv.ui.settings.screen.screensaver.SettingsScreensaverModeScreen
import org.jellyfin.androidtv.ui.settings.screen.screensaver.SettingsScreensaverScreen
import org.jellyfin.androidtv.ui.settings.screen.screensaver.SettingsScreensaverTimeoutScreen

val customizationRoutes =
	mapOf<String, RouteComposable>(
		Routes.CUSTOMIZATION to {
			ScreenIdOverlay(ScreenIds.SETTINGS_CUSTOMIZATION_ID, ScreenIds.SETTINGS_CUSTOMIZATION_NAME) {
				SettingsCustomizationScreen()
			}
		},
		Routes.CUSTOMIZATION_THEME to {
			SettingsCustomizationThemeScreen()
		},
		Routes.CUSTOMIZATION_CLOCK to {
			SettingsCustomizationClockScreen()
		},
		Routes.CUSTOMIZATION_WATCHED_INDICATOR to {
			SettingsCustomizationWatchedIndicatorScreen()
		},
		Routes.CUSTOMIZATION_SCREENSAVER to {
			ScreenIdOverlay(ScreenIds.SETTINGS_SCREENSAVER_ID, ScreenIds.SETTINGS_SCREENSAVER_NAME) {
				SettingsScreensaverScreen()
			}
		},
		Routes.CUSTOMIZATION_SCREENSAVER_TIMEOUT to {
			SettingsScreensaverTimeoutScreen()
		},
		Routes.CUSTOMIZATION_SCREENSAVER_AGE_RATING to {
			SettingsScreensaverAgeRatingScreen()
		},
		Routes.CUSTOMIZATION_SCREENSAVER_MODE to {
			SettingsScreensaverModeScreen()
		},
		Routes.CUSTOMIZATION_SCREENSAVER_DIMMING to {
			SettingsScreensaverDimmingScreen()
		},
		Routes.CUSTOMIZATION_SUBTITLES to {
			ScreenIdOverlay(ScreenIds.SETTINGS_SUBTITLES_ID, ScreenIds.SETTINGS_SUBTITLES_NAME) {
				SettingsSubtitlesScreen()
			}
		},
		Routes.CUSTOMIZATION_SUBTITLES_TEXT_COLOR to {
			SettingsSubtitlesTextColorScreen()
		},
		Routes.CUSTOMIZATION_SUBTITLES_BACKGROUND_COLOR to {
			SettingsSubtitlesBackgroundColorScreen()
		},
		Routes.CUSTOMIZATION_SUBTITLES_EDGE_COLOR to {
			SettingsSubtitleTextStrokeColorScreen()
		},
	)
