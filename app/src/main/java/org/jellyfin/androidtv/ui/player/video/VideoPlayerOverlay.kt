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

@Composable
fun VideoPlayerOverlay(
	modifier: Modifier = Modifier,
	playbackManager: PlaybackManager = koinInject(),
	mediaToastRegistry: MediaToastRegistry,
) {
	val visibilityState = rememberPlayerOverlayVisibility()

	val entry by rememberQueueEntry(playbackManager)
	val item = entry?.run { baseItemFlow.collectAsState(baseItem) }?.value

	PlayerOverlayLayout(
		visibilityState = visibilityState,
		modifier = modifier,
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
		onSeekForward = {
			playbackManager.state.fastForward()
			mediaToastRegistry.emit(VegafoXIcons.FastForward)
		},
		onSeekBackward = {
			playbackManager.state.rewind()
			mediaToastRegistry.emit(VegafoXIcons.Rewind)
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
			)
		},
	)

	MediaToasts(mediaToastRegistry)
}
