package org.jellyfin.androidtv.ui.settings.screen.playback

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.ExternalAppRepository
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.LocalShapes
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.form.Checkbox
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.list.ListButton
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.navigation.LocalRouter
import org.jellyfin.androidtv.ui.settings.Routes
import org.jellyfin.androidtv.ui.settings.compat.rememberPreference
import org.jellyfin.androidtv.ui.settings.composable.SettingsColumn
import org.koin.compose.koinInject

@Composable
fun SettingsPlaybackScreen(
	userPreferences: UserPreferences = koinInject(),
	externalAppRepository: ExternalAppRepository = koinInject(),
) {
	val context = LocalContext.current
	val router = LocalRouter.current

	SettingsColumn {
		item {
			Text(
				text = stringResource(R.string.pref_playback),
				fontFamily = BebasNeue,
				fontSize = 22.sp,
				color = VegafoXColors.TextPrimary,
				modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.TvPlay), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.playback_video_player)) },
				trailingContent = {
					val iconDrawable =
						remember(context) {
							externalAppRepository.getCurrentExternalPlayerApp(context)?.loadIcon(context.packageManager)
						}
					Image(
						painter =
							if (iconDrawable == null) {
								rememberAsyncImagePainter(R.mipmap.app_icon)
							} else {
								rememberAsyncImagePainter(iconDrawable)
							},
						contentDescription = null,
						modifier =
							Modifier
								.size(24.dp)
								.clip(LocalShapes.current.small),
					)
				},
				onClick = { router.push(Routes.PLAYBACK_PLAYER) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.NextUp), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback_next_up)) },
				onClick = { router.push(Routes.PLAYBACK_NEXT_UP) },
			)
		}

		item {
			var stillWatchingBehavior by rememberPreference(userPreferences, UserPreferences.stillWatchingBehavior)
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Sleep), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback_inactivity_prompt)) },
				captionContent = { Text(stringResource(stillWatchingBehavior.nameRes)) },
				onClick = { router.push(Routes.PLAYBACK_INACTIVITY_PROMPT) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Trailer), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback_prerolls)) },
				onClick = { router.push(Routes.PLAYBACK_PREROLLS) },
			)
		}

		// TODO: Subtitles screen needs to be recreated (SettingsSubtitlesScreen.kt not found)
		// item {
		// 	ListButton(
		// 		leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Subtitles), contentDescription = null) },
		// 		headingContent = { Text(stringResource(R.string.pref_customization_subtitles)) },
		// 		onClick = { router.push(Routes.CUSTOMIZATION_SUBTITLES) },
		// 	)
		// }

		item {
			var subtitlesDefaultToNone by rememberPreference(userPreferences, UserPreferences.subtitlesDefaultToNone)
			ListButton(
				headingContent = { Text(stringResource(R.string.pref_subtitles_default_to_none)) },
				captionContent = { Text(stringResource(R.string.pref_subtitles_default_to_none_description)) },
				trailingContent = { Checkbox(checked = subtitlesDefaultToNone) },
				onClick = { subtitlesDefaultToNone = !subtitlesDefaultToNone },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.Clapperboard), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback_media_segments)) },
				onClick = { router.push(Routes.PLAYBACK_MEDIA_SEGMENTS) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.SyncPlay), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.syncplay)) },
				captionContent = { Text(stringResource(R.string.syncplay_description)) },
				onClick = { router.push(Routes.VEGAFOX_SYNCPLAY) },
			)
		}

		item {
			ListButton(
				leadingContent = { Icon(rememberVectorPainter(VegafoXIcons.MoreHoriz), contentDescription = null) },
				headingContent = { Text(stringResource(R.string.pref_playback_advanced)) },
				onClick = { router.push(Routes.PLAYBACK_ADVANCED) },
			)
		}
	}
}
