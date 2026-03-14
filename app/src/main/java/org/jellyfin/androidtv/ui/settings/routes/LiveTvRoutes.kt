package org.jellyfin.androidtv.ui.settings.routes

import org.jellyfin.androidtv.ui.navigation.RouteComposable
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.screen.livetv.SettingsLiveTvGuideChannelOrderScreen
import org.jellyfin.androidtv.ui.settings.screen.livetv.SettingsLiveTvGuideFiltersScreen
import org.jellyfin.androidtv.ui.settings.screen.livetv.SettingsLiveTvGuideOptionsScreen

val liveTvRoutes =
	mapOf<String, RouteComposable>(
		Routes.LIVETV_GUIDE_FILTERS to {
			SettingsLiveTvGuideFiltersScreen()
		},
		Routes.LIVETV_GUIDE_OPTIONS to {
			SettingsLiveTvGuideOptionsScreen()
		},
		Routes.LIVETV_GUIDE_CHANNEL_ORDER to {
			SettingsLiveTvGuideChannelOrderScreen()
		},
	)
