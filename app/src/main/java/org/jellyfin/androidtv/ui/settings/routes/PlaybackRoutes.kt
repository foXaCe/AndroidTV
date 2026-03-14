package org.jellyfin.androidtv.ui.settings.routes

import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.navigation.RouteComposable
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.composable.SettingsNumericScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackAdvancedScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackAudioBehaviorScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackInactivityPromptScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackMaxBitrateScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackMaxResolutionScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackPlayerScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackPrerollsScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackRefreshRateSwitchingBehaviorScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackResumeSubtractDurationScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.SettingsPlaybackZoomModeScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.mediasegment.SettingsPlaybackMediaSegmentScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.mediasegment.SettingsPlaybackMediaSegmentsScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.nextup.SettingsPlaybackNextUpBehaviorScreen
import org.jellyfin.androidtv.ui.settings.screen.playback.nextup.SettingsPlaybackNextUpScreen
import org.jellyfin.sdk.model.api.MediaSegmentType

val playbackRoutes =
	mapOf<String, RouteComposable>(
		Routes.PLAYBACK to {
			ScreenIdOverlay(ScreenIds.SETTINGS_PLAYBACK_ID, ScreenIds.SETTINGS_PLAYBACK_NAME) {
				SettingsPlaybackScreen()
			}
		},
		Routes.PLAYBACK_PLAYER to {
			SettingsPlaybackPlayerScreen()
		},
		Routes.PLAYBACK_NEXT_UP to {
			SettingsPlaybackNextUpScreen()
		},
		Routes.PLAYBACK_NEXT_UP_BEHAVIOR to {
			SettingsPlaybackNextUpBehaviorScreen()
		},
		Routes.PLAYBACK_INACTIVITY_PROMPT to {
			SettingsPlaybackInactivityPromptScreen()
		},
		Routes.PLAYBACK_PREROLLS to {
			SettingsPlaybackPrerollsScreen()
		},
		Routes.PLAYBACK_MEDIA_SEGMENTS to {
			SettingsPlaybackMediaSegmentsScreen()
		},
		Routes.PLAYBACK_MEDIA_SEGMENT to { context ->
			val segmentType = context.parameters["segmentType"]?.let(MediaSegmentType::fromNameOrNull)
			if (segmentType != null) {
				SettingsPlaybackMediaSegmentScreen(segmentType = segmentType)
			}
		},
		Routes.PLAYBACK_ADVANCED to {
			ScreenIdOverlay(ScreenIds.SETTINGS_PLAYBACK_ADV_ID, ScreenIds.SETTINGS_PLAYBACK_ADV_NAME) {
				SettingsPlaybackAdvancedScreen()
			}
		},
		Routes.PLAYBACK_RESUME_SUBTRACT_DURATION to {
			SettingsPlaybackResumeSubtractDurationScreen()
		},
		Routes.PLAYBACK_MAX_BITRATE to {
			SettingsPlaybackMaxBitrateScreen()
		},
		Routes.PLAYBACK_MAX_RESOLUTION to {
			SettingsPlaybackMaxResolutionScreen()
		},
		Routes.PLAYBACK_REFRESH_RATE_SWITCHING_BEHAVIOR to {
			SettingsPlaybackRefreshRateSwitchingBehaviorScreen()
		},
		Routes.PLAYBACK_ZOOM_MODE to {
			SettingsPlaybackZoomModeScreen()
		},
		Routes.PLAYBACK_AUDIO_BEHAVIOR to {
			SettingsPlaybackAudioBehaviorScreen()
		},
	)

val syncPlayRoutes =
	mapOf<String, RouteComposable>(
		Routes.VEGAFOX_SYNCPLAY_MIN_DELAY to {
			SettingsNumericScreen(
				route = Routes.VEGAFOX_SYNCPLAY_MIN_DELAY,
				preference = UserPreferences.syncPlayMinDelaySpeedToSync,
				titleRes = R.string.pref_syncplay_min_delay_speed_to_sync,
				valueTemplate = R.string.pref_syncplay_min_delay_speed_to_sync_description,
				minValue = 10.0,
				maxValue = 1000.0,
				stepSize = 10.0,
			)
		},
		Routes.VEGAFOX_SYNCPLAY_MAX_DELAY to {
			SettingsNumericScreen(
				route = Routes.VEGAFOX_SYNCPLAY_MAX_DELAY,
				preference = UserPreferences.syncPlayMaxDelaySpeedToSync,
				titleRes = R.string.pref_syncplay_max_delay_speed_to_sync,
				valueTemplate = R.string.pref_syncplay_max_delay_speed_to_sync_description,
				minValue = 10.0,
				maxValue = 1000.0,
				stepSize = 10.0,
			)
		},
		Routes.VEGAFOX_SYNCPLAY_DURATION to {
			SettingsNumericScreen(
				route = Routes.VEGAFOX_SYNCPLAY_DURATION,
				preference = UserPreferences.syncPlaySpeedToSyncDuration,
				titleRes = R.string.pref_syncplay_speed_to_sync_duration,
				valueTemplate = R.string.pref_syncplay_speed_to_sync_duration_description,
				minValue = 500.0,
				maxValue = 5000.0,
				stepSize = 100.0,
			)
		},
		Routes.VEGAFOX_SYNCPLAY_MIN_DELAY_SKIP to {
			SettingsNumericScreen(
				route = Routes.VEGAFOX_SYNCPLAY_MIN_DELAY_SKIP,
				preference = UserPreferences.syncPlayMinDelaySkipToSync,
				titleRes = R.string.pref_syncplay_min_delay_skip_to_sync,
				valueTemplate = R.string.pref_syncplay_min_delay_skip_to_sync_description,
				minValue = 10.0,
				maxValue = 5000.0,
				stepSize = 10.0,
			)
		},
		Routes.VEGAFOX_SYNCPLAY_EXTRA_OFFSET to {
			SettingsNumericScreen(
				route = Routes.VEGAFOX_SYNCPLAY_EXTRA_OFFSET,
				preference = UserPreferences.syncPlayExtraTimeOffset,
				titleRes = R.string.pref_syncplay_extra_time_offset,
				valueTemplate = R.string.pref_syncplay_extra_time_offset_description,
				minValue = -1000.0,
				maxValue = 1000.0,
				stepSize = 10.0,
			)
		},
	)
