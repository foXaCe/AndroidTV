package org.jellyfin.androidtv.ui.settings.routes

import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.navigation.RouteComposable
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.screen.SettingsDeveloperScreen
import org.jellyfin.androidtv.ui.settings.screen.SettingsMainScreen
import org.jellyfin.androidtv.ui.settings.screen.SettingsTelemetryScreen
import org.jellyfin.androidtv.ui.settings.screen.about.SettingsAboutScreen
import org.jellyfin.androidtv.ui.settings.screen.license.SettingsLicenseScreen
import org.jellyfin.androidtv.ui.settings.screen.license.SettingsLicensesScreen

val allSettingsRoutes: Map<String, RouteComposable> =
	mapOf<String, RouteComposable>(
		Routes.MAIN to {
			ScreenIdOverlay(ScreenIds.SETTINGS_MAIN_ID, ScreenIds.SETTINGS_MAIN_NAME) {
				SettingsMainScreen()
			}
		},
		Routes.TELEMETRY to {
			ScreenIdOverlay(ScreenIds.SETTINGS_TELEMETRY_ID, ScreenIds.SETTINGS_TELEMETRY_NAME) {
				SettingsTelemetryScreen()
			}
		},
		Routes.DEVELOPER to {
			ScreenIdOverlay(ScreenIds.SETTINGS_DEVELOPER_ID, ScreenIds.SETTINGS_DEVELOPER_NAME) {
				SettingsDeveloperScreen()
			}
		},
		Routes.ABOUT to { context ->
			ScreenIdOverlay(ScreenIds.SETTINGS_ABOUT_ID, ScreenIds.SETTINGS_ABOUT_NAME) {
				SettingsAboutScreen(context.parameters["fromLogin"] == "true")
			}
		},
		Routes.LICENSES to {
			SettingsLicensesScreen()
		},
		Routes.LICENSE to { context ->
			val artifactId = context.parameters["artifactId"]
			if (artifactId != null) {
				SettingsLicenseScreen(artifactId = artifactId)
			}
		},
	) +
		authenticationRoutes +
		customizationRoutes +
		libraryRoutes +
		homeRoutes +
		liveTvRoutes +
		playbackRoutes +
		syncPlayRoutes +
		vegafoXRoutes
