package org.jellyfin.androidtv.ui.playback.overlay.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Seekbar
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.ButtonDefaults
import org.jellyfin.androidtv.ui.base.components.TvIconButton
import org.jellyfin.androidtv.ui.base.theme.TvSpacing
import org.jellyfin.androidtv.util.TimeUtils
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PlayerOverlayScreen(
	state: PlayerOverlayState,
	onAction: (PlayerOverlayAction) -> Unit,
	modifier: Modifier = Modifier,
) {
	AnimatedVisibility(
		visible = state.isVisible,
		enter = fadeIn(tween(200)),
		exit = fadeOut(tween(200)),
	) {
		Box(modifier = modifier.fillMaxSize()) {
			// Bottom controls
			Column(
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.fillMaxWidth()
					.background(
						brush = Brush.verticalGradient(
							colors = listOf(
								Color.Transparent,
								Color.Black.copy(alpha = 0.85f),
							)
						)
					)
					.padding(
						horizontal = TvSpacing.screenHorizontal,
						vertical = TvSpacing.screenVertical,
					)
			) {
				// Time row
				PlayerTimeRow(state = state)

				Spacer(Modifier.height(8.dp))

				// Seekbar
				PlayerSeekBar(state = state, onAction = onAction)

				Spacer(Modifier.height(16.dp))

				// Primary controls
				PlayerPrimaryControls(state = state, onAction = onAction)

				Spacer(Modifier.height(8.dp))

				// Secondary controls
				PlayerSecondaryControls(state = state, onAction = onAction)
			}
		}
	}
}

@Composable
private fun PlayerTimeRow(state: PlayerOverlayState) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = TimeUtils.formatMillis(state.position),
			style = JellyfinTheme.typography.labelMedium,
			color = Color.White,
		)
		if (state.endTime.isNotEmpty()) {
			Text(
				text = state.endTime,
				style = JellyfinTheme.typography.labelMedium,
				color = Color.White.copy(alpha = 0.7f),
			)
		} else {
			Text(
				text = TimeUtils.formatMillis(state.duration),
				style = JellyfinTheme.typography.labelMedium,
				color = Color.White.copy(alpha = 0.7f),
			)
		}
	}
}

@Composable
private fun PlayerSeekBar(
	state: PlayerOverlayState,
	onAction: (PlayerOverlayAction) -> Unit,
) {
	val duration = state.duration.coerceAtLeast(1L)
	Seekbar(
		progress = state.position.milliseconds,
		buffer = state.buffered.milliseconds,
		duration = duration.milliseconds,
		onSeek = { progress -> onAction(PlayerOverlayAction.SeekTo(progress.inWholeMilliseconds)) },
		enabled = state.canSeek,
		modifier = Modifier
			.fillMaxWidth()
			.height(6.dp),
	)
}

@Composable
private fun PlayerPrimaryControls(
	state: PlayerOverlayState,
	onAction: (PlayerOverlayAction) -> Unit,
) {
	val buttonColors = ButtonDefaults.colors(
		containerColor = Color.White.copy(alpha = 0.15f),
		contentColor = Color.White,
		focusedContainerColor = Color.White.copy(alpha = 0.3f),
		focusedContentColor = Color.White,
	)

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically,
	) {
		if (state.hasPreviousItem) {
			TvIconButton(
				icon = ImageVector.vectorResource(R.drawable.ic_previous),
				contentDescription = stringResource(R.string.lbl_prev_item),
				onClick = { onAction(PlayerOverlayAction.SkipPrevious) },
				tint = Color.White,
				colors = buttonColors,
			)
			Spacer(Modifier.width(12.dp))
		}

		if (state.canSeek) {
			TvIconButton(
				icon = ImageVector.vectorResource(R.drawable.ic_rewind),
				contentDescription = stringResource(R.string.rewind),
				onClick = { onAction(PlayerOverlayAction.Rewind) },
				tint = Color.White,
				colors = buttonColors,
			)
			Spacer(Modifier.width(12.dp))
		}

		// Play/Pause — larger
		TvIconButton(
			icon = ImageVector.vectorResource(
				if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
			),
			contentDescription = stringResource(
				if (state.isPlaying) R.string.lbl_pause else R.string.lbl_play
			),
			onClick = { onAction(PlayerOverlayAction.PlayPause) },
			tint = Color.White,
			colors = buttonColors,
			modifier = Modifier.size(56.dp),
		)

		if (state.canSeek) {
			Spacer(Modifier.width(12.dp))
			TvIconButton(
				icon = ImageVector.vectorResource(R.drawable.ic_fast_forward),
				contentDescription = stringResource(R.string.fast_forward),
				onClick = { onAction(PlayerOverlayAction.FastForward) },
				tint = Color.White,
				colors = buttonColors,
			)
		}

		if (state.hasNextItem) {
			Spacer(Modifier.width(12.dp))
			TvIconButton(
				icon = ImageVector.vectorResource(R.drawable.ic_next),
				contentDescription = stringResource(R.string.lbl_next_item),
				onClick = { onAction(PlayerOverlayAction.SkipNext) },
				tint = Color.White,
				colors = buttonColors,
			)
		}
	}
}

@Composable
private fun PlayerSecondaryControls(
	state: PlayerOverlayState,
	onAction: (PlayerOverlayAction) -> Unit,
) {
	val buttonColors = ButtonDefaults.colors(
		containerColor = Color.Transparent,
		contentColor = Color.White.copy(alpha = 0.8f),
		focusedContainerColor = Color.White.copy(alpha = 0.15f),
		focusedContentColor = Color.White,
	)

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically,
	) {
		if (state.hasSubs) {
			SecondaryButton(
				icon = R.drawable.ic_select_subtitle,
				description = R.string.lbl_subtitle_track,
				onClick = { onAction(PlayerOverlayAction.ShowSubtitles) },
				colors = buttonColors,
			)
		}

		if (state.hasMultiAudio) {
			SecondaryButton(
				icon = R.drawable.ic_select_audio,
				description = R.string.lbl_audio_track,
				onClick = { onAction(PlayerOverlayAction.ShowAudio) },
				colors = buttonColors,
			)
		}

		if (state.isLiveTv) {
			SecondaryButton(
				icon = R.drawable.ic_channel_bar,
				description = R.string.lbl_other_channels,
				onClick = { onAction(PlayerOverlayAction.ShowChannels) },
				colors = buttonColors,
			)
			SecondaryButton(
				icon = R.drawable.ic_guide,
				description = R.string.lbl_live_tv_guide,
				onClick = { onAction(PlayerOverlayAction.ShowGuide) },
				colors = buttonColors,
			)
		}

		if (state.isLiveTv && state.canRecordLiveTv) {
			val recordIcon = if (state.isRecording) R.drawable.ic_record_red else R.drawable.ic_record
			SecondaryButton(
				icon = recordIcon,
				description = R.string.lbl_record,
				onClick = { onAction(PlayerOverlayAction.ToggleRecord) },
				colors = buttonColors,
			)
		}

		if (state.hasChapters) {
			SecondaryButton(
				icon = R.drawable.ic_select_chapter,
				description = R.string.lbl_chapters,
				onClick = { onAction(PlayerOverlayAction.ShowChapters) },
				colors = buttonColors,
			)
		}

		if (state.hasCast) {
			SecondaryButton(
				icon = R.drawable.ic_cast_list,
				description = R.string.lbl_cast,
				onClick = { onAction(PlayerOverlayAction.ShowCast) },
				colors = buttonColors,
			)
		}

		if (!state.isLiveTv) {
			SecondaryButton(
				icon = R.drawable.ic_playback_speed,
				description = R.string.lbl_playback_speed,
				onClick = { onAction(PlayerOverlayAction.ShowSpeed) },
				colors = buttonColors,
			)
			SecondaryButton(
				icon = R.drawable.ic_select_quality,
				description = R.string.lbl_quality_profile,
				onClick = { onAction(PlayerOverlayAction.ShowQuality) },
				colors = buttonColors,
			)
			SecondaryButton(
				icon = R.drawable.ic_audio_sync,
				description = R.string.lbl_audio_delay,
				onClick = { onAction(PlayerOverlayAction.ShowAudioDelay) },
				colors = buttonColors,
			)
		}

		if (state.hasSubs) {
			SecondaryButton(
				icon = R.drawable.ic_subtitle_sync,
				description = R.string.lbl_subtitle_delay,
				onClick = { onAction(PlayerOverlayAction.ShowSubtitleDelay) },
				colors = buttonColors,
			)
		}

		SecondaryButton(
			icon = R.drawable.ic_aspect_ratio,
			description = R.string.lbl_zoom,
			onClick = { onAction(PlayerOverlayAction.ShowZoom) },
			colors = buttonColors,
		)
	}
}

@Composable
private fun SecondaryButton(
	icon: Int,
	description: Int,
	onClick: () -> Unit,
	colors: org.jellyfin.androidtv.ui.base.button.ButtonColors,
) {
	TvIconButton(
		icon = ImageVector.vectorResource(icon),
		contentDescription = stringResource(description),
		onClick = onClick,
		tint = Color.White.copy(alpha = 0.8f),
		colors = colors,
		modifier = Modifier.size(40.dp),
	)
}
