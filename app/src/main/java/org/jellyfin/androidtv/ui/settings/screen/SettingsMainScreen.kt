package org.jellyfin.androidtv.ui.settings.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
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

	var updateInfoForDialog by remember { mutableStateOf<UpdateCheckerService.UpdateInfo?>(null) }
	var showReleaseNotes by remember { mutableStateOf(false) }

	SettingsColumn {
		// ── Header (logo + title, fixed 48dp max) ──
		item {
			Row(
				modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp).height(48.dp),
				horizontalArrangement = Arrangement.spacedBy(14.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Image(
					painter = painterResource(R.drawable.ic_vegafox_fox),
					contentDescription = null,
					modifier = Modifier.size(36.dp).clip(CircleShape),
				)
				Column {
					Text(
						text = "VegafoX",
						fontFamily = BebasNeue,
						fontSize = 20.sp,
						letterSpacing = 2.sp,
						color = VegafoXColors.OrangePrimary,
					)
					Text(
						text = stringResource(R.string.settings),
						fontSize = 12.sp,
						color = VegafoXColors.TextSecondary,
					)
				}
			}
		}

		// ── Apparence ──
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_appearance)) }) }

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Tune), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_customization)) },
				trailingContent = {
					Icon(
						rememberVectorPainter(VegafoXIcons.ArrowForward),
						contentDescription = null,
						tint = VegafoXColors.TextSecondary,
					)
				},
				onClick = { router.push(Routes.CUSTOMIZATION) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.PhotoLibrary), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_screensaver)) },
				trailingContent = {
					Icon(
						rememberVectorPainter(VegafoXIcons.ArrowForward),
						contentDescription = null,
						tint = VegafoXColors.TextSecondary,
					)
				},
				onClick = { router.push(Routes.CUSTOMIZATION_SCREENSAVER) },
			)
		}

		// ── Lecture ──
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_playback)) }) }

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.SkipNext), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback)) },
				trailingContent = {
					Icon(
						rememberVectorPainter(VegafoXIcons.ArrowForward),
						contentDescription = null,
						tint = VegafoXColors.TextSecondary,
					)
				},
				onClick = { router.push(Routes.PLAYBACK) },
			)
		}

		// ── Extensions ──
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_plugin_settings)) }) }

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Tune), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_plugin_settings)) },
				captionContent = { Text(stringResource(R.string.pref_plugin_description)) },
				trailingContent = {
					Icon(
						rememberVectorPainter(VegafoXIcons.ArrowForward),
						contentDescription = null,
						tint = VegafoXColors.TextSecondary,
					)
				},
				onClick = { router.push(Routes.PLUGIN) },
			)
		}

		// ── Compte ──
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_login)) }) }

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Group), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_login)) },
				trailingContent = {
					Icon(
						rememberVectorPainter(VegafoXIcons.ArrowForward),
						contentDescription = null,
						tint = VegafoXColors.TextSecondary,
					)
				},
				onClick = { router.push(Routes.AUTHENTICATION) },
			)
		}

		// ── Informations ──
		item { ListSection(headingContent = { Text(stringResource(R.string.pref_about_title)) }) }

		if (org.jellyfin.androidtv.BuildConfig.ENABLE_OTA_UPDATES) {
			item {
				ListButton(
					leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Download), contentDescription = null) },
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
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Science), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_developer_link)) },
				trailingContent = {
					Icon(
						rememberVectorPainter(VegafoXIcons.ArrowForward),
						contentDescription = null,
						tint = VegafoXColors.TextSecondary,
					)
				},
				onClick = { router.push(Routes.DEVELOPER) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(painterResource(R.drawable.ic_jellyfin), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_about_title)) },
				trailingContent = {
					Icon(
						rememberVectorPainter(VegafoXIcons.ArrowForward),
						contentDescription = null,
						tint = VegafoXColors.TextSecondary,
					)
				},
				onClick = { router.push(Routes.ABOUT) },
			)
		}
	}

	// Dialogs
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
