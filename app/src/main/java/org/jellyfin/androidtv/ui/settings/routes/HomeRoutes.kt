package org.jellyfin.androidtv.ui.settings.routes

import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.navigation.RouteComposable
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.screen.home.SettingsHomePosterSizeScreen
import org.jellyfin.androidtv.ui.settings.screen.home.SettingsHomeScreen
import org.jellyfin.androidtv.ui.settings.screen.vegafox.SettingsVegafoXHomeRowsImageScreen

val homeRoutes =
	mapOf<String, RouteComposable>(
		Routes.HOME to {
			ScreenIdOverlay(ScreenIds.SETTINGS_HOME_ID, ScreenIds.SETTINGS_HOME_NAME) {
				SettingsHomeScreen()
			}
		},
		Routes.HOME_POSTER_SIZE to {
			ScreenIdOverlay(ScreenIds.SETTINGS_HOME_POSTER_ID, ScreenIds.SETTINGS_HOME_POSTER_NAME) {
				SettingsHomePosterSizeScreen()
			}
		},
		Routes.HOME_ROWS_IMAGE_TYPE to {
			ScreenIdOverlay(ScreenIds.SETTINGS_HOME_ROWS_IMG_ID, ScreenIds.SETTINGS_HOME_ROWS_IMG_NAME) {
				SettingsVegafoXHomeRowsImageScreen()
			}
		},
	)
