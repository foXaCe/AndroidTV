package org.jellyfin.androidtv.ui.player.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.jellyfin.androidtv.ui.base.Seekbar
import org.jellyfin.androidtv.ui.base.SeekbarColors
import org.jellyfin.androidtv.ui.base.SeekbarDefaults
import org.jellyfin.androidtv.ui.composable.rememberPlayerProgress
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import org.koin.compose.koinInject
import kotlin.time.Duration
import kotlin.time.times

@Composable
fun PlayerSeekbar(
	modifier: Modifier = Modifier,
	colors: SeekbarColors = SeekbarDefaults.colors(),
	playbackManager: PlaybackManager = koinInject<PlaybackManager>(),
	onScrubPosition: ((Long) -> Unit)? = null,
	thumbnailContent: (@Composable (fraction: Float) -> Unit)? = null,
) {
	val playState by playbackManager.state.playState.collectAsState()
	val positionInfo = playbackManager.state.positionInfo

	// Optimistic progress: shows the seek target immediately, cleared after 500ms
	var optimisticTarget by remember { mutableStateOf<Duration?>(null) }
	if (optimisticTarget != null) {
		LaunchedEffect(optimisticTarget) {
			delay(500)
			optimisticTarget = null
		}
	}

	val progress =
		rememberPlayerProgress(
			playing = playState == PlayState.PLAYING,
			active = positionInfo.active,
			duration = positionInfo.duration,
		)

	val effectiveProgress =
		if (optimisticTarget != null && positionInfo.duration > Duration.ZERO) {
			optimisticTarget!!
		} else {
			progress.toDouble() * positionInfo.duration
		}

	val seekForwardAmount = remember { playbackManager.options.defaultFastForwardAmount() }
	val seekRewindAmount = remember { playbackManager.options.defaultRewindAmount() }

	Seekbar(
		progress = effectiveProgress,
		buffer = positionInfo.buffer,
		duration = positionInfo.duration,
		seekForwardAmount = seekForwardAmount,
		seekRewindAmount = seekRewindAmount,
		onScrubbing = { scrubbing ->
			playbackManager.state.setScrubbing(scrubbing)
			if (!scrubbing) onScrubPosition?.invoke(-1)
		},
		onSeek = { seekProgress ->
			optimisticTarget = seekProgress
			playbackManager.state.seek(seekProgress)
			onScrubPosition?.invoke(seekProgress.inWholeMilliseconds)
		},
		modifier = modifier,
		colors = colors,
		enabled = positionInfo.duration > Duration.ZERO,
		thumbnailContent = thumbnailContent,
	)
}
