package org.jellyfin.androidtv.ui.player.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.composable.rememberQueueEntry
import org.jellyfin.androidtv.ui.player.base.PlayerOverlayLayout
import org.jellyfin.androidtv.ui.player.base.rememberPlayerOverlayVisibility
import org.jellyfin.androidtv.ui.player.base.toast.MediaToastRegistry
import org.jellyfin.androidtv.ui.player.base.toast.MediaToasts
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import org.jellyfin.playback.jellyfin.queue.baseItem
import org.jellyfin.playback.jellyfin.queue.baseItemFlow
import org.koin.compose.koinInject
import kotlin.time.times

@Composable
fun VideoPlayerOverlay(
	modifier: Modifier = Modifier,
	playbackManager: PlaybackManager = koinInject(),
	mediaToastRegistry: MediaToastRegistry,
	onSeekFeedback: ((SeekFeedbackDirection, Int) -> Unit)? = null,
) {
	val visibilityState = rememberPlayerOverlayVisibility()

	val entry by rememberQueueEntry(playbackManager)
	val item = entry?.run { baseItemFlow.collectAsState(baseItem) }?.value

	val durationMinutes = playbackManager.state.positionInfo.duration.inWholeMinutes
	val baseForwardAmount = playbackManager.options.defaultFastForwardAmount()
	val baseRewindAmount = playbackManager.options.defaultRewindAmount()

	PlayerOverlayLayout(
		visibilityState = visibilityState,
		modifier = modifier,
		contentDurationMinutes = durationMinutes,
		onPlayPause = {
			when (playbackManager.state.playState.value) {
				PlayState.PLAYING -> {
					playbackManager.state.pause()
					mediaToastRegistry.emit(VegafoXIcons.Pause)
				}
				PlayState.PAUSED -> {
					playbackManager.state.unpause()
					mediaToastRegistry.emit(VegafoXIcons.Play)
				}
				PlayState.STOPPED, PlayState.ERROR -> {
					playbackManager.state.play()
					mediaToastRegistry.emit(VegafoXIcons.Play)
				}
			}
		},
		onSeekForward = { multiplier ->
			playbackManager.state.fastForward(multiplier * baseForwardAmount)
			mediaToastRegistry.emit(VegafoXIcons.FastForward)
			val seconds = (multiplier * baseForwardAmount).inWholeSeconds.toInt()
			onSeekFeedback?.invoke(SeekFeedbackDirection.FORWARD, seconds)
		},
		onSeekBackward = { multiplier ->
			playbackManager.state.rewind(multiplier * baseRewindAmount)
			mediaToastRegistry.emit(VegafoXIcons.Rewind)
			val seconds = (multiplier * baseRewindAmount).inWholeSeconds.toInt()
			onSeekFeedback?.invoke(SeekFeedbackDirection.REWIND, seconds)
		},
		header = {
			VideoPlayerHeader(
				item = item,
			)
		},
		controls = {
			VideoPlayerControls(
				playbackManager = playbackManager,
				item = item,
				onSeekFeedback = onSeekFeedback,
			)
		},
	)

	MediaToasts(mediaToastRegistry)
}
