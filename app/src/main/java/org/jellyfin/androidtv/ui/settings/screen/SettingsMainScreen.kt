package org.jellyfin.androidtv.ui.settings.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.UpdateCheckerService
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.form.Checkbox
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.preference.category.DonateDialog
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.compat.rememberPreference
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.inject

@Composable
fun SettingsMainScreen() {
	val router = LocalRouter.current
	val context = LocalContext.current
	val updateChecker by inject<UpdateCheckerService>(UpdateCheckerService::class.java)
	val userPreferences = koinInject<UserPreferences>()

	var showDonateDialog by remember { mutableStateOf(false) }
	var updateInfoForDialog by remember { mutableStateOf<UpdateCheckerService.UpdateInfo?>(null) }
	var showReleaseNotes by remember { mutableStateOf(false) }

	SettingsColumn {
		item {
			ListSection(
				overlineContent = { Text(stringResource(R.string.app_name).uppercase()) },
				headingContent = { Text(stringResource(R.string.settings)) },
				captionContent = { Text(stringResource(R.string.settings_description)) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_users), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_login)) },
				onClick = { router.push(Routes.AUTHENTICATION) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_adjust), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_customization)) },
				onClick = { router.push(Routes.CUSTOMIZATION) }
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_vegafox), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_plugin_settings)) },
				captionContent = { Text(stringResource(R.string.pref_plugin_description)) },
				onClick = { router.push(Routes.PLUGIN) }
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_photos), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_screensaver)) },
				onClick = { router.push(Routes.CUSTOMIZATION_SCREENSAVER) }
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_next), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback)) },
				onClick = { router.push(Routes.PLAYBACK) }
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_error), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_telemetry_category)) },
				onClick = { router.push(Routes.TELEMETRY) }
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_flask), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_developer_link)) },
				onClick = { router.push(Routes.DEVELOPER) }
			)
		}

		item {
			ListSection(
				headingContent = { Text(stringResource(R.string.settings_support_updates)) },
			)
		}

		if (org.jellyfin.androidtv.BuildConfig.ENABLE_OTA_UPDATES) {
			item {
				ListButton(
					leadingContent = {
						Icon(
							painterResource(R.drawable.ic_get_app),
							contentDescription = null
						)
					},
					headingContent = { Text(stringResource(R.string.settings_check_updates)) },
					captionContent = { Text(stringResource(R.string.settings_check_updates_desc)) },
					onClick = {
						checkForUpdates(context, updateChecker) { info ->
							updateInfoForDialog = info
						}
					}
				)
			}

			item {
				var updateNotificationsEnabled by rememberPreference(userPreferences, UserPreferences.updateNotificationsEnabled)
				ListButton(
					headingContent = { Text(stringResource(R.string.settings_update_notifications)) },
					captionContent = { Text(stringResource(R.string.settings_update_notifications_desc)) },
					trailingContent = { Checkbox(checked = updateNotificationsEnabled) },
					onClick = { updateNotificationsEnabled = !updateNotificationsEnabled }
				)
			}
		}

		item {
			ListButton(
				leadingContent = {
					Icon(
						painterResource(R.drawable.ic_heart),
						contentDescription = null,
						tint = Color.Red
					)
				},
				headingContent = { Text(stringResource(R.string.settings_support_vegafox)) },
				captionContent = { Text(stringResource(R.string.settings_support_vegafox_desc)) },
				onClick = {
					showDonateDialog = true
				}
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_jellyfin), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_about_title)) },
				onClick = { router.push(Routes.ABOUT) }
			)
		}
	}

	// Dialogs
	if (showDonateDialog) {
		DonateDialog(onDismiss = { showDonateDialog = false })
	}

	val currentUpdateInfo = updateInfoForDialog
	if (currentUpdateInfo != null && !showReleaseNotes) {
		UpdateAvailableDialog(
			updateInfo = currentUpdateInfo,
			onDownload = {
				updateInfoForDialog = null
				downloadAndInstall(context, updateChecker, currentUpdateInfo)
			},
			onReleaseNotes = { showReleaseNotes = true },
			onDismiss = { updateInfoForDialog = null },
		)
	}

	if (currentUpdateInfo != null && showReleaseNotes) {
		ReleaseNotesDialog(
			updateInfo = currentUpdateInfo,
			onDownload = {
				showReleaseNotes = false
				updateInfoForDialog = null
				downloadAndInstall(context, updateChecker, currentUpdateInfo)
			},
			onViewOnGitHub = {
				openUrl(context, currentUpdateInfo.releaseUrl)
			},
			onDismiss = {
				showReleaseNotes = false
				updateInfoForDialog = null
			},
		)
	}
}
