package org.jellyfin.androidtv.ui.settings.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.UpdateCheckerService
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.form.Checkbox
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.list.ListSection
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.preference.category.DonateDialog
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.compat.rememberPreference
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.koin.compose.koinInject

@Composable
fun SettingsMainScreen(
	userPreferences: UserPreferences = koinInject(),
	updateChecker: UpdateCheckerService = koinInject(),
) {
	val router = LocalRouter.current
	val context = LocalContext.current

	var showDonateDialog by remember { mutableStateOf(false) }
	var updateInfoForDialog by remember { mutableStateOf<UpdateCheckerService.UpdateInfo?>(null) }
	var showReleaseNotes by remember { mutableStateOf(false) }

	SettingsColumn {
		item {
			Row(
				modifier = Modifier.padding(24.dp),
				horizontalArrangement = Arrangement.spacedBy(16.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Image(
					painter = painterResource(R.drawable.ic_vegafox_fox),
					contentDescription = null,
					modifier =
						Modifier
							.size(40.dp)
							.clip(CircleShape),
				)
				Column {
					Text(
						text = "VegafoX",
						fontFamily = BebasNeue,
						fontSize = 22.sp,
						letterSpacing = 2.sp,
						color = VegafoXColors.OrangePrimary,
					)
					Text(
						text = stringResource(R.string.settings),
						fontSize = 13.sp,
						color = VegafoXColors.TextSecondary,
					)
				}
			}
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Group), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_login)) },
				onClick = { router.push(Routes.AUTHENTICATION) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Tune), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_customization)) },
				onClick = { router.push(Routes.CUSTOMIZATION) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_vegafox_fox), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_plugin_settings)) },
				captionContent = { Text(stringResource(R.string.pref_plugin_description)) },
				onClick = { router.push(Routes.PLUGIN) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.PhotoLibrary), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_screensaver)) },
				onClick = { router.push(Routes.CUSTOMIZATION_SCREENSAVER) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.SkipNext), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback)) },
				onClick = { router.push(Routes.PLAYBACK) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Error), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_telemetry_category)) },
				onClick = { router.push(Routes.TELEMETRY) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Science), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_developer_link)) },
				onClick = { router.push(Routes.DEVELOPER) },
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
							rememberVectorPainter(VegafoXIcons.Download),
							contentDescription = null,
						)
					},
					headingContent = { Text(stringResource(R.string.settings_check_updates)) },
					captionContent = { Text(stringResource(R.string.settings_check_updates_desc)) },
					onClick = {
						checkForUpdates(context, updateChecker) { info ->
							updateInfoForDialog = info
						}
					},
				)
			}

			item {
				var updateNotificationsEnabled by rememberPreference(userPreferences, UserPreferences.updateNotificationsEnabled)
				ListButton(
					headingContent = { Text(stringResource(R.string.settings_update_notifications)) },
					captionContent = { Text(stringResource(R.string.settings_update_notifications_desc)) },
					trailingContent = { Checkbox(checked = updateNotificationsEnabled) },
					onClick = { updateNotificationsEnabled = !updateNotificationsEnabled },
				)
			}
		}

		item {
			ListButton(
				leadingContent = {
					Icon(
						rememberVectorPainter(VegafoXIcons.Favorite),
						contentDescription = null,
						tint = Color.Red,
					)
				},
				headingContent = { Text(stringResource(R.string.settings_support_vegafox)) },
				captionContent = { Text(stringResource(R.string.settings_support_vegafox_desc)) },
				onClick = {
					showDonateDialog = true
				},
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_jellyfin), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_about_title)) },
				onClick = { router.push(Routes.ABOUT) },
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
