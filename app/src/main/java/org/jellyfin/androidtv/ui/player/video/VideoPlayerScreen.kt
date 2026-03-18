package org.jellyfin.androidtv.ui.player.video

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.data.service.BackgroundService
import org.jellyfin.androidtv.ui.ScreensaverLock
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.composable.rememberPlayerPositionInfo
import org.jellyfin.androidtv.ui.composable.rememberQueueEntry
import org.jellyfin.androidtv.ui.playback.overlay.SkipOverlay
import org.jellyfin.androidtv.ui.playback.segment.MediaSegmentAction
import org.jellyfin.androidtv.ui.playback.segment.MediaSegmentRepository
import org.jellyfin.androidtv.ui.player.base.PlayerSubtitles
import org.jellyfin.androidtv.ui.player.base.PlayerSurface
import org.jellyfin.androidtv.ui.player.base.toast.MediaToastRegistry
import org.jellyfin.androidtv.ui.player.video.toast.rememberPlaybackManagerMediaToastEmitter
import org.jellyfin.androidtv.ui.shared.components.VegafoXLoadingFox
import org.jellyfin.androidtv.util.apiclient.getLogoImage
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.sdk.end
import org.jellyfin.androidtv.util.sdk.start
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import org.jellyfin.playback.jellyfin.queue.baseItem
import org.jellyfin.playback.jellyfin.queue.baseItemFlow
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.MediaSegmentDto
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.seconds

private const val DefaultVideoAspectRatio = 16f / 9f

@Composable
fun VideoPlayerScreen() {
	val playbackManager = koinInject<PlaybackManager>()
	val mediaSegmentRepository = koinInject<MediaSegmentRepository>()

	val backgroundService = koinInject<BackgroundService>()
	LaunchedEffect(backgroundService) {
		backgroundService.clearBackgrounds()
	}

	val playing by remember {
		playbackManager.state.playState.map { it == PlayState.PLAYING }
	}.collectAsState(false)
	ScreensaverLock(
		enabled = playing,
	)

	val videoSize by playbackManager.state.videoSize.collectAsState()
	val aspectRatio = videoSize.aspectRatio.takeIf { !it.isNaN() && it > 0f } ?: DefaultVideoAspectRatio

	val coroutineScope = rememberCoroutineScope()
	val mediaToastRegistry = remember { MediaToastRegistry(coroutineScope) }
	rememberPlaybackManagerMediaToastEmitter(playbackManager, mediaToastRegistry)

	// -- Seek feedback state (accumulated delta, auto-dismiss after 600ms) --
	var seekFeedback by remember { mutableStateOf<SeekFeedback?>(null) }
	var seekFeedbackJob by remember { mutableStateOf<Job?>(null) }
	val onSeekFeedback: (SeekFeedbackDirection, Int) -> Unit = { direction, delta ->
		seekFeedbackJob?.cancel()
		seekFeedback =
			if (seekFeedback?.direction == direction) {
				SeekFeedback(direction, seekFeedback!!.deltaSeconds + delta)
			} else {
				SeekFeedback(direction, delta)
			}
		seekFeedbackJob =
			coroutineScope.launch {
				delay(600)
				seekFeedback = null
			}
	}

	// -- Media segments: fetch for current item, track active segment --
	val entry by rememberQueueEntry(playbackManager)
	val item = entry?.run { baseItemFlow.collectAsState(baseItem) }?.value

	var segments by remember { mutableStateOf<List<MediaSegmentDto>>(emptyList()) }
	LaunchedEffect(item?.id) {
		if (item == null) {
			segments = emptyList()
			return@LaunchedEffect
		}
		var result = emptyList<MediaSegmentDto>()
		repeat(3) { attempt ->
			result = mediaSegmentRepository.getSegmentsForItem(item)
			if (result.isNotEmpty()) return@repeat
			if (attempt < 2) delay(1000)
		}
		segments = result
	}

	val positionInfo by rememberPlayerPositionInfo(playbackManager, precision = 1.seconds)
	val activeSegment by remember {
		derivedStateOf {
			val pos = positionInfo.active
			segments.firstOrNull { segment ->
				val action = mediaSegmentRepository.getMediaSegmentAction(segment)
				(action == MediaSegmentAction.ASK_TO_SKIP || action == MediaSegmentAction.SKIP) &&
					pos >= segment.start &&
					pos < segment.end
			}
		}
	}

	Box(
		modifier =
			Modifier
				.background(Color.Black)
				.fillMaxSize(),
	) {
		PlayerSurface(
			playbackManager = playbackManager,
			modifier =
				Modifier
					.aspectRatio(aspectRatio, videoSize.height < videoSize.width)
					.fillMaxSize()
					.align(Alignment.Center),
		)

		VideoPlayerOverlay(
			playbackManager = playbackManager,
			mediaToastRegistry = mediaToastRegistry,
			onSeekFeedback = onSeekFeedback,
		)

		// Seek feedback overlays (forward / rewind delta indicator)
		SeekFeedbackOverlay(
			feedback = seekFeedback?.takeIf { it.direction == SeekFeedbackDirection.FORWARD },
			modifier =
				Modifier
					.align(Alignment.CenterEnd)
					.padding(end = 120.dp),
		)
		SeekFeedbackOverlay(
			feedback = seekFeedback?.takeIf { it.direction == SeekFeedbackDirection.REWIND },
			modifier =
				Modifier
					.align(Alignment.CenterStart)
					.padding(start = 120.dp),
		)

		// Skip overlay for media segments (intro, outro, recap, etc.)
		SkipOverlay(
			visible = activeSegment != null,
			segmentType = activeSegment?.type?.name,
			nextEpisodeTitle = null,
			timeRemaining = null,
			onSkip = {
				activeSegment?.let { segment ->
					playbackManager.state.seek(segment.end)
				}
			},
			modifier =
				Modifier
					.align(Alignment.BottomEnd)
					.padding(end = 56.dp, bottom = 96.dp),
		)

		PlayerSubtitles(
			playbackManager = playbackManager,
			modifier =
				Modifier
					.aspectRatio(aspectRatio, videoSize.height < videoSize.width)
					.fillMaxSize()
					.align(Alignment.Center),
		)

		// Logo overlay: fixed 2.5s animation then dismiss.
		// Video loads in parallel underneath — no wasted time.
		val isBuffering by playbackManager.state.isBuffering.collectAsState()
		var showLoadingOverlay by remember { mutableStateOf(true) }
		LaunchedEffect(item?.id) {
			showLoadingOverlay = true
			delay(1500)
			showLoadingOverlay = false
		}

		// Logo loading overlay (initial load — before first frame)
		val api = koinInject<ApiClient>()
		AnimatedVisibility(
			visible = showLoadingOverlay,
			enter = fadeIn(tween(0)),
			exit = fadeOut(tween(300)),
			modifier = Modifier.fillMaxSize(),
		) {
			PlaybackLoadingOverlay(
				logoUrl = item?.getLogoImage()?.getUrl(api, maxWidth = 600),
				title = item?.name,
			)
		}

		// Mid-playback buffering fox (only after logo overlay is gone)
		AnimatedVisibility(
			visible = isBuffering && !showLoadingOverlay,
			enter = fadeIn(),
			exit = fadeOut(),
			modifier = Modifier.align(Alignment.Center),
		) {
			VegafoXLoadingFox()
		}
	}
}

// ── Playback loading overlay (logo zoom) ─────────────────────────────────

@Composable
private fun PlaybackLoadingOverlay(
	logoUrl: String?,
	title: String?,
) {
	val scale = remember { Animatable(1.0f) }
	val alpha = remember { Animatable(1.0f) }

	LaunchedEffect(Unit) {
		launch {
			scale.animateTo(5.0f, tween(durationMillis = 1500, easing = EaseInCubic))
		}
		alpha.animateTo(0.0f, tween(durationMillis = 1500, easing = EaseInCubic))
	}

	Box(
		modifier =
			Modifier
				.fillMaxSize()
				.background(VegafoXColors.BackgroundDeep),
		contentAlignment = Alignment.Center,
	) {
		if (logoUrl != null) {
			Image(
				painter = rememberAsyncImagePainter(logoUrl),
				contentDescription = null,
				contentScale = ContentScale.Fit,
				modifier =
					Modifier
						.widthIn(max = 400.dp)
						.graphicsLayer {
							scaleX = scale.value
							scaleY = scale.value
							this.alpha = alpha.value
						},
			)
		} else if (!title.isNullOrBlank()) {
			Text(
				text = title,
				style =
					TextStyle(
						fontFamily = BebasNeue,
						fontSize = 48.sp,
						color = VegafoXColors.TextPrimary,
						textAlign = TextAlign.Center,
					),
				modifier =
					Modifier
						.widthIn(max = 400.dp)
						.graphicsLayer {
							scaleX = scale.value
							scaleY = scale.value
							this.alpha = alpha.value
						},
			)
		}
	}
}

// ── Seek feedback overlay ────────────────────────────────────────────────

@Composable
private fun SeekFeedbackOverlay(
	feedback: SeekFeedback?,
	modifier: Modifier = Modifier,
) {
	// Keep the last non-null value for the exit animation
	var lastFeedback by remember { mutableStateOf(feedback) }
	if (feedback != null) lastFeedback = feedback

	AnimatedVisibility(
		visible = feedback != null,
		enter = fadeIn(tween(80)),
		exit = fadeOut(tween(300)),
		modifier = modifier,
	) {
		lastFeedback?.let { fb ->
			val isForward = fb.direction == SeekFeedbackDirection.FORWARD
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier =
					Modifier
						.background(
							VegafoXColors.BackgroundDeep.copy(alpha = 0.75f),
							RoundedCornerShape(12.dp),
						).padding(horizontal = 16.dp, vertical = 10.dp),
			) {
				Icon(
					imageVector = if (isForward) VegafoXIcons.Forward30 else VegafoXIcons.Replay10,
					contentDescription = null,
					tint = VegafoXColors.OrangePrimary,
					modifier = Modifier.size(32.dp),
				)
				Spacer(Modifier.width(8.dp))
				Text(
					text = if (isForward) "+${fb.deltaSeconds}s" else "-${fb.deltaSeconds}s",
					style =
						TextStyle(
							fontFamily = BebasNeue,
							fontSize = 28.sp,
							color = VegafoXColors.TextPrimary,
						),
				)
			}
		}
	}
}
