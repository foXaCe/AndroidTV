package org.jellyfin.androidtv.ui.settings.routes

import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.navigation.RouteComposable
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.screen.authentication.SettingsAuthenticationAutoSignInScreen
import org.jellyfin.androidtv.ui.settings.screen.authentication.SettingsAuthenticationPinCodeScreen
import org.jellyfin.androidtv.ui.settings.screen.authentication.SettingsAuthenticationScreen
import org.jellyfin.androidtv.ui.settings.screen.authentication.SettingsAuthenticationServerScreen
import org.jellyfin.androidtv.ui.settings.screen.authentication.SettingsAuthenticationServerUserScreen
import org.jellyfin.androidtv.ui.settings.screen.authentication.SettingsAuthenticationSortByScreen
import org.jellyfin.sdk.model.serializer.toUUIDOrNull

val authenticationRoutes =
	mapOf<String, RouteComposable>(
		Routes.AUTHENTICATION to {
			ScreenIdOverlay(ScreenIds.SETTINGS_AUTH_ID, ScreenIds.SETTINGS_AUTH_NAME) {
				SettingsAuthenticationScreen(false)
			}
		},
		Routes.AUTHENTICATION_FROM_LOGIN to {
			ScreenIdOverlay(ScreenIds.SETTINGS_AUTH_ID, ScreenIds.SETTINGS_AUTH_NAME) {
				SettingsAuthenticationScreen(true)
			}
		},
		Routes.AUTHENTICATION_SERVER to { context ->
			val serverId = context.parameters["serverId"]?.toUUIDOrNull()
			if (serverId != null) {
				SettingsAuthenticationServerScreen(serverId = serverId)
			}
		},
		Routes.AUTHENTICATION_SERVER_USER to { context ->
			val serverId = context.parameters["serverId"]?.toUUIDOrNull()
			val userId = context.parameters["userId"]?.toUUIDOrNull()
			if (serverId != null && userId != null) {
				SettingsAuthenticationServerUserScreen(
					serverId = serverId,
					userId = userId,
				)
			}
		},
		Routes.AUTHENTICATION_SORT_BY to {
			SettingsAuthenticationSortByScreen()
		},
		Routes.AUTHENTICATION_AUTO_SIGN_IN to {
			SettingsAuthenticationAutoSignInScreen()
		},
		Routes.AUTHENTICATION_PIN_CODE to {
			SettingsAuthenticationPinCodeScreen()
		},
	)
