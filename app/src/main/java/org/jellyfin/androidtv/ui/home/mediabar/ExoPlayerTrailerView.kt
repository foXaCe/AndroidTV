package org.jellyfin.androidtv.ui.home.mediabar

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import timber.log.Timber

/**
 * Composable that renders a YouTube trailer preview using ExoPlayer
 * with stream URLs resolved via NewPipe Extractor.
 *
 * Uses [TextureView] instead of PlayerView to support true transparency
 * (no opaque SurfaceView layer), enabling seamless gradient-masked
 * compositing over the backdrop.
 */
@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerTrailerView(
	streamInfo: YouTubeStreamResolver.StreamInfo,
	startSeconds: Double,
	segments: List<SponsorBlockApi.Segment>,
	muted: Boolean = false,
	onVideoEnded: () -> Unit = {},
	onVideoReady: () -> Unit = {},
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	var player by remember { mutableStateOf<ExoPlayer?>(null) }
	var videoSize by remember { mutableStateOf<VideoSize?>(null) }
	val mainHandler = remember { Handler(Looper.getMainLooper()) }
	val skipRunnable = remember { mutableStateOf<Runnable?>(null) }

	// Sync mute state dynamically
	LaunchedEffect(muted) {
		player?.volume = if (muted) 0f else 1f
	}

	DisposableEffect(streamInfo.videoUrl) {
		Timber.d(
			"TRAILER_DBG: ExoPlayerTrailerView creating player, videoUrl=${streamInfo.videoUrl.take(
				80,
			)}, audioUrl=${streamInfo.audioUrl?.take(80)}, isVideoOnly=${streamInfo.isVideoOnly}",
		)
		val exoPlayer =
			buildTrailerPlayer(
				context = context,
				streamInfo = streamInfo,
				startSeconds = startSeconds,
				muted = muted,
				onVideoReady = {
					Timber.d("TRAILER_DBG: ExoPlayer onVideoReady!")
					onVideoReady()
				},
				onVideoEnded = {
					Timber.d("TRAILER_DBG: ExoPlayer onVideoEnded!")
					onVideoEnded()
				},
				onVideoSizeChanged = { size ->
					videoSize = size
				},
			)

		player = exoPlayer

		if (segments.isNotEmpty()) {
			val runnable =
				object : Runnable {
					override fun run() {
						val p = player ?: return
						if (!p.isPlaying) {
							mainHandler.postDelayed(this, 500)
							return
						}
						val currentSec = p.currentPosition / 1000.0
						for (seg in segments) {
							if (currentSec >= seg.startTime && currentSec < seg.endTime - 0.5) {
								p.seekTo((seg.endTime * 1000).toLong())
								break
							}
						}
						mainHandler.postDelayed(this, 500)
					}
				}
			skipRunnable.value = runnable
			mainHandler.postDelayed(runnable, 500)
		}

		onDispose {
			skipRunnable.value?.let { mainHandler.removeCallbacks(it) }
			skipRunnable.value = null
			exoPlayer.pause()
			exoPlayer.stop()
			exoPlayer.release()
			player = null
			videoSize = null
		}
	}

	val currentPlayer = player
	val currentVideoSize = videoSize
	if (currentPlayer != null) {
		AndroidView(
			factory = { ctx ->
				TextureView(ctx).also { tv ->
					currentPlayer.setVideoTextureView(tv)
					currentPlayer.addListener(
						object : Player.Listener {
							override fun onVideoSizeChanged(size: VideoSize) {
								tv.post { applyCenterCrop(tv, size) }
							}
						},
					)
				}
			},
			update = { tv ->
				currentPlayer.setVideoTextureView(tv)
				if (currentVideoSize != null) {
					applyCenterCrop(tv, currentVideoSize)
				}
			},
			modifier = modifier.fillMaxSize(),
		)
	}
}

/**
 * Center-crop (zoom) transform — scales video to completely fill the
 * TextureView, cropping any excess. Equivalent to RESIZE_MODE_ZOOM.
 */
private fun applyCenterCrop(
	textureView: TextureView,
	videoSize: VideoSize,
) {
	val vw = videoSize.width.toFloat()
	val vh = videoSize.height.toFloat()
	if (vw == 0f || vh == 0f) return
	val tw = textureView.width.toFloat()
	val th = textureView.height.toFloat()
	if (tw == 0f || th == 0f) return

	val videoAspect = (vw * videoSize.pixelWidthHeightRatio) / vh
	val viewAspect = tw / th

	val sx: Float
	val sy: Float
	if (videoAspect > viewAspect) {
		// Video wider than view — scale to height, crop sides
		sy = 1f
		sx = videoAspect / viewAspect
	} else {
		// Video taller than view — scale to width, crop top/bottom
		sx = 1f
		sy = viewAspect / videoAspect
	}

	val matrix = android.graphics.Matrix()
	matrix.setScale(sx, sy, tw / 2f, th / 2f)
	textureView.setTransform(matrix)
}

@OptIn(UnstableApi::class)
private fun buildTrailerPlayer(
	context: Context,
	streamInfo: YouTubeStreamResolver.StreamInfo,
	startSeconds: Double,
	muted: Boolean,
	onVideoReady: () -> Unit,
	onVideoEnded: () -> Unit,
	onVideoSizeChanged: (VideoSize) -> Unit,
): ExoPlayer {
	val dataSourceFactory = DefaultHttpDataSource.Factory()

	val player =
		ExoPlayer
			.Builder(context)
			.setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
			.build()

	player.volume = if (muted) 0f else 1f
	player.repeatMode = Player.REPEAT_MODE_OFF
	player.playWhenReady = true

	player.trackSelectionParameters =
		player.trackSelectionParameters
			.buildUpon()
			.setMaxVideoSize(1920, 1080)
			.build()

	var readySignaled = false

	player.addListener(
		object : Player.Listener {
			override fun onPlaybackStateChanged(playbackState: Int) {
				when (playbackState) {
					Player.STATE_READY -> {
						if (!readySignaled) {
							readySignaled = true
							onVideoReady()
						}
					}
					Player.STATE_ENDED -> {
						onVideoEnded()
					}
				}
			}

			override fun onPlayerError(error: PlaybackException) {
				Timber.w(error, "TRAILER_DBG: ExoPlayer onPlayerError code=${error.errorCode} message=${error.message}")
				onVideoEnded()
			}

			override fun onVideoSizeChanged(videoSize: VideoSize) {
				onVideoSizeChanged(videoSize)
			}
		},
	)

	if (streamInfo.isVideoOnly && streamInfo.audioUrl != null) {
		val videoSource =
			ProgressiveMediaSource
				.Factory(dataSourceFactory)
				.createMediaSource(MediaItem.fromUri(streamInfo.videoUrl))
		val audioSource =
			ProgressiveMediaSource
				.Factory(dataSourceFactory)
				.createMediaSource(MediaItem.fromUri(streamInfo.audioUrl))
		val merged = MergingMediaSource(videoSource, audioSource)
		player.setMediaSource(merged)
	} else {
		player.setMediaItem(MediaItem.fromUri(streamInfo.videoUrl))
	}

	player.prepare()

	if (startSeconds > 0) {
		player.seekTo((startSeconds * 1000).toLong())
	}

	return player
}
