package org.jellyfin.androidtv.ui.playback

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.core.graphics.TypefaceCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.TsExtractor
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.compat.StreamInfo
import org.jellyfin.androidtv.data.syncplay.SyncPlayManager
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.MediaStream
import org.jellyfin.sdk.model.api.MediaStreamType
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import timber.log.Timber

@OptIn(UnstableApi::class)
class VideoManager(
	private val mActivity: Activity,
	view: View,
	private val userPreferences: UserPreferences,
	private val syncPlayManager: SyncPlayManager,
	private val exoPlayerHttpDataSourceFactory: HttpDataSource.Factory,
	private val onScreensaverLock: ((Boolean) -> Unit)? = null,
	private val onFatalError: (() -> Unit)? = null,
) {
	var mExoPlayer: ExoPlayer? = null
		private set
	private var mExoPlayerView: PlayerView
	private var mCustomSubtitleView: SubtitleView
	private val mHandler = Handler(Looper.getMainLooper())
	private var mAudioDelayProcessor: AudioDelayProcessor
	private var mSubtitleDelayHandler: SubtitleDelayHandler?

	private var mMetaDuration: Long = -1
	private var lastExoPlayerPosition: Long = -1
	private val nightModeEnabled: Boolean

	var isContracted = false

	var zoomMode: ZoomMode = ZoomMode.FIT
		private set

	var normalWidth = 0
		private set
	var normalHeight = 0
		private set

	private var mPlaybackControllerNotifiable: PlaybackControllerNotifiable? = null

	private var mAudioDelayMs: Long = 0

	init {
		nightModeEnabled = userPreferences[UserPreferences.audioNightMode]

		mExoPlayer = configureExoplayerBuilder(mActivity).build()

		if (userPreferences[UserPreferences.debuggingEnabled]) {
			mExoPlayer!!.addAnalyticsListener(EventLogger())
		}

		// Volume normalisation (audio night mode).
		if (nightModeEnabled) {
			mExoPlayer!!.addAnalyticsListener(
				object : AnalyticsListener {
					override fun onAudioSessionIdChanged(
						eventTime: AnalyticsListener.EventTime,
						audioSessionId: Int,
					) {
						applyAudioNightmode(audioSessionId)
					}
				},
			)
		}

		mExoPlayerView = view.findViewById(R.id.exoPlayerView)

		// Hide PlayerView's built-in subtitle view to prevent conflicts with our custom delay handling
		mExoPlayerView.subtitleView?.visibility = View.GONE

		mExoPlayerView.player = mExoPlayer
		val strokeColor = userPreferences[UserPreferences.subtitleTextStrokeColor].toInt()
		val textWeight = userPreferences[UserPreferences.subtitlesTextWeight]
		val subtitleStyle =
			CaptionStyleCompat(
				userPreferences[UserPreferences.subtitlesTextColor].toInt(),
				userPreferences[UserPreferences.subtitlesBackgroundColor].toInt(),
				Color.TRANSPARENT,
				if (Color.alpha(strokeColor) == 0) CaptionStyleCompat.EDGE_TYPE_NONE else CaptionStyleCompat.EDGE_TYPE_OUTLINE,
				strokeColor,
				TypefaceCompat.create(mActivity, Typeface.DEFAULT, textWeight, false),
			)

		// Create our own custom SubtitleView that we can control independently
		mCustomSubtitleView = SubtitleView(mActivity)
		mCustomSubtitleView.setFixedTextSize(TypedValue.COMPLEX_UNIT_DIP, userPreferences[UserPreferences.subtitlesTextSize])
		mCustomSubtitleView.setApplyEmbeddedFontSizes(false)
		mCustomSubtitleView.setApplyEmbeddedStyles(false)
		mCustomSubtitleView.setBottomPaddingFraction(userPreferences[UserPreferences.subtitlesOffsetPosition])
		mCustomSubtitleView.setStyle(subtitleStyle)

		// Add custom subtitle view as overlay on top of PlayerView
		val subtitleParams =
			FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT,
			)
		(mExoPlayerView.parent as FrameLayout).addView(mCustomSubtitleView, subtitleParams)

		// Initialize subtitle delay handler with our custom subtitle view
		mSubtitleDelayHandler = SubtitleDelayHandler(mCustomSubtitleView)
		mExoPlayer!!.addListener(mSubtitleDelayHandler!!)

		// Subtitle position and size patch for wide aspect ratio videos (PR #4816)
		mExoPlayer!!.addListener(
			object : Player.Listener {
				override fun onVideoSizeChanged(videoSize: VideoSize) {
					if (videoSize.height == 0 || videoSize.width == 0) return
					val subtitleView = mCustomSubtitleView
					var videoHeight = videoSize.height
					val videoWidth = videoSize.width
					val aspectRatio = videoWidth.toFloat() / videoHeight
					if (aspectRatio < 1.78f) return
					if (videoHeight != mExoPlayerView.height) {
						videoHeight = (mExoPlayerView.width / aspectRatio).toInt()
					}
					val subslp = subtitleView.layoutParams as FrameLayout.LayoutParams
					val verticalMargins = mExoPlayerView.height - videoHeight
					subslp.height = mExoPlayerView.height
					subslp.topMargin = verticalMargins / (-2)
					subtitleView.layoutParams = subslp
					val parent = mExoPlayerView.parent as FrameLayout
					parent.clipChildren = false
					parent.clipToPadding = false
					if (parent.parent is ViewGroup) {
						(parent.parent as ViewGroup).clipChildren = false
						(parent.parent as ViewGroup).clipToPadding = false
					}
					mExoPlayerView.clipChildren = false
				}
			},
		)

		mExoPlayer!!.addListener(
			object : Player.Listener {
				override fun onPlayerError(error: PlaybackException) {
					Timber.e(error, "***** Player error: code=%d message=%s", error.errorCode, error.message)
					error.cause?.let { Timber.e(it, "***** Player error cause") }
					mPlaybackControllerNotifiable?.onError()
					stopProgressLoop()
				}

				override fun onIsPlayingChanged(isPlaying: Boolean) {
					if (isPlaying) {
						mPlaybackControllerNotifiable?.onPrepared()
						startProgressLoop()
						onScreensaverLock?.invoke(true)
					} else {
						stopProgressLoop()
						onScreensaverLock?.invoke(false)
					}
				}

				override fun onPlaybackStateChanged(playbackState: Int) {
					if (playbackState == Player.STATE_BUFFERING) {
						syncPlayManager.reportBuffering()
					}
					if (playbackState == Player.STATE_READY) {
						syncPlayManager.reportReady()
					}
					if (playbackState == Player.STATE_ENDED) {
						mPlaybackControllerNotifiable?.onCompletion()
						stopProgressLoop()
					}
				}

				override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
					mPlaybackControllerNotifiable?.onPlaybackSpeedChange(playbackParameters.speed)
				}

				override fun onPositionDiscontinuity(
					oldPosition: Player.PositionInfo,
					newPosition: Player.PositionInfo,
					reason: Int,
				) {
					if (reason == Player.DISCONTINUITY_REASON_INTERNAL) {
						Timber.i("Caught player discontinuity (reason internal) - oldPos: %s newPos: %s", oldPosition.positionMs, newPosition.positionMs)
					}
				}

				override fun onTimelineChanged(
					timeline: androidx.media3.common.Timeline,
					reason: Int,
				) {
					// no-op
				}
			},
		)

		// Initialize audio delay processor
		mAudioDelayProcessor = AudioDelayProcessor()
	}

	fun subscribe(notifier: PlaybackControllerNotifiable) {
		mPlaybackControllerNotifiable = notifier
	}

	private fun determineExoPlayerExtensionRendererMode(): Int =
		if (userPreferences[UserPreferences.preferExoPlayerFfmpeg]) {
			DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
		} else {
			DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
		}

	private fun configureExoplayerBuilder(context: android.content.Context): ExoPlayer.Builder {
		val exoPlayerBuilder = ExoPlayer.Builder(context)

		// Create audio delay processor
		mAudioDelayProcessor = AudioDelayProcessor()

		// Create custom renderers factory
		val defaultRendererFactory =
			object : DefaultRenderersFactory(context) {
				override fun buildAudioSink(
					context: android.content.Context,
					enableFloatOutput: Boolean,
					enableAudioTrackPlaybackParams: Boolean,
				): androidx.media3.exoplayer.audio.AudioSink =
					androidx.media3.exoplayer.audio.DefaultAudioSink
						.Builder(context)
						.setEnableFloatOutput(enableFloatOutput)
						.setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
						.setAudioProcessors(
							arrayOf(
								androidx.media3.common.audio
									.SonicAudioProcessor(),
								androidx.media3.exoplayer.audio
									.SilenceSkippingAudioProcessor(),
								mAudioDelayProcessor,
							),
						).build()
			}
		defaultRendererFactory.setEnableDecoderFallback(true)
		defaultRendererFactory.setExtensionRendererMode(determineExoPlayerExtensionRendererMode())

		val trackSelector = DefaultTrackSelector(context)
		trackSelector.setParameters(
			trackSelector
				.buildUponParameters()
				.setAudioOffloadPreferences(
					TrackSelectionParameters.AudioOffloadPreferences
						.Builder()
						.setAudioOffloadMode(TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED)
						.build(),
				).setAllowInvalidateSelectionsOnRendererCapabilitiesChange(true)
				.build(),
		)
		exoPlayerBuilder.setTrackSelector(trackSelector)

		val extractorsFactory =
			DefaultExtractorsFactory()
				.setTsExtractorTimestampSearchBytes(TsExtractor.DEFAULT_TIMESTAMP_SEARCH_BYTES * 3)
		extractorsFactory.setConstantBitrateSeekingEnabled(true)
		extractorsFactory.setConstantBitrateSeekingAlwaysEnabled(true)
		val dataSourceFactory = DefaultDataSource.Factory(context, exoPlayerHttpDataSourceFactory)
		exoPlayerBuilder.setRenderersFactory(defaultRendererFactory)
		exoPlayerBuilder.setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory, extractorsFactory))

		exoPlayerBuilder.setAudioAttributes(
			AudioAttributes
				.Builder()
				.setUsage(C.USAGE_MEDIA)
				.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
				.build(),
			true,
		)

		return exoPlayerBuilder
	}

	fun isInitialized(): Boolean = mExoPlayer != null

	fun setZoom(mode: ZoomMode) {
		zoomMode = mode
		when (mode) {
			ZoomMode.FIT -> mExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
			ZoomMode.AUTO_CROP -> mExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)
			ZoomMode.STRETCH -> mExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)
		}
	}

	fun setMetaDuration(duration: Long) {
		mMetaDuration = duration
	}

	fun getDuration(): Long {
		val player = mExoPlayer
		return if (player != null && player.duration > 0) player.duration else mMetaDuration
	}

	fun getBufferedPosition(): Long {
		val player = mExoPlayer ?: return -1
		val bufferedPosition = player.bufferedPosition
		return if (bufferedPosition > -1 && bufferedPosition < getDuration()) bufferedPosition else -1
	}

	fun seekWithinBuffer(pos: Long): Boolean {
		val player = mExoPlayer ?: return false
		val currentPos = player.currentPosition
		val bufferedEnd = player.bufferedPosition
		Timber.i("Attempting seek from %d to %d (buffered up to: %d)", currentPos, pos, bufferedEnd)
		player.seekTo(pos)
		return true
	}

	fun getCurrentPosition(): Long {
		val player = mExoPlayer
		return if (player == null || !isPlaying()) {
			if (lastExoPlayerPosition == -1L) 0 else lastExoPlayerPosition
		} else {
			val pos = player.currentPosition
			lastExoPlayerPosition = pos
			pos
		}
	}

	fun isPlaying(): Boolean = mExoPlayer?.isPlaying == true

	fun start() {
		val player = mExoPlayer
		if (player == null) {
			Timber.e("mExoPlayer should not be null!!")
			onFatalError?.invoke()
			return
		}
		player.playWhenReady = true
		normalWidth = mExoPlayerView.layoutParams.width
		normalHeight = mExoPlayerView.layoutParams.height
	}

	fun play() {
		mExoPlayer?.playWhenReady = true
	}

	fun pause() {
		mExoPlayer?.playWhenReady = false
	}

	fun stopPlayback() {
		mExoPlayer?.let { player ->
			player.stop()
			player.trackSelectionParameters =
				player.trackSelectionParameters
					.buildUpon()
					.clearOverridesOfType(C.TRACK_TYPE_AUDIO)
					.build()
		}
		stopProgressLoop()
	}

	fun isSeekable(): Boolean {
		val player = mExoPlayer ?: return false
		val canSeek = player.isCurrentMediaItemSeekable
		Timber.d("current media item is%s seekable", if (canSeek) "" else " not")
		return canSeek
	}

	fun seekTo(pos: Long): Long {
		val player = mExoPlayer ?: return -1
		Timber.i("Exo length in seek is: %d", getDuration())
		player.seekTo(pos)
		return pos
	}

	private fun getSubtitleSelectionFlags(mediaStream: MediaStream): Int {
		var flags = 0
		if (mediaStream.isDefault) flags = flags and C.SELECTION_FLAG_DEFAULT
		if (mediaStream.isForced) flags = flags and C.SELECTION_FLAG_FORCED
		return flags
	}

	fun setMediaStreamInfo(
		api: ApiClient,
		streamInfo: StreamInfo,
	) {
		val path = streamInfo.mediaUrl
		if (path == null) {
			Timber.w("Video path is null cannot continue")
			return
		}
		Timber.i("Video path set to: %s", path)

		try {
			val subtitleConfigurations = mutableListOf<MediaItem.SubtitleConfiguration>()
			for (mediaStream in streamInfo.mediaSource?.mediaStreams.orEmpty()) {
				if (mediaStream.type != MediaStreamType.SUBTITLE) continue

				if (mediaStream.deliveryMethod == SubtitleDeliveryMethod.EXTERNAL) {
					val deliveryUrl = mediaStream.deliveryUrl ?: continue
					val subtitleUri = Uri.parse(api.createUrl(deliveryUrl, emptyMap(), emptyMap(), true))
					val subtitleConfiguration =
						MediaItem.SubtitleConfiguration
							.Builder(subtitleUri)
							.setId("JF_EXTERNAL:" + mediaStream.index.toString())
							.setMimeType(getSubtitleMediaStreamCodec(mediaStream))
							.setLanguage(mediaStream.language)
							.setLabel(mediaStream.displayTitle)
							.setSelectionFlags(getSubtitleSelectionFlags(mediaStream))
							.build()
					Timber.i("Adding subtitle track %s of type %s", subtitleConfiguration.uri, subtitleConfiguration.mimeType)
					subtitleConfigurations.add(subtitleConfiguration)
				}
			}

			val mediaItem =
				MediaItem
					.Builder()
					.setUri(Uri.parse(path))
					.setSubtitleConfigurations(subtitleConfigurations)
					.build()

			mExoPlayer?.setMediaItem(mediaItem)
			mExoPlayer?.prepare()
		} catch (e: IllegalStateException) {
			Timber.e(e, "Unable to set video path.  Probably backing out.")
		}
	}

	private fun offsetStreamIndex(
		index: Int,
		adjustByAdding: Boolean,
		allStreams: List<MediaStream>?,
	): Int {
		if (index < 0 || allStreams == null) return -1

		var result = index
		for (stream in allStreams) {
			if (!stream.isExternal) break
			result += if (adjustByAdding) 1 else -1
		}

		return if (result < 0 || result > allStreams.size) -1 else result
	}

	fun getExoPlayerTrack(
		streamType: MediaStreamType?,
		allStreams: List<MediaStream>?,
	): Int {
		val player = mExoPlayer
		if (player == null || streamType == null || allStreams == null) return -1
		if (streamType != MediaStreamType.SUBTITLE && streamType != MediaStreamType.AUDIO) return -1

		val chosenTrackType = if (streamType == MediaStreamType.SUBTITLE) C.TRACK_TYPE_TEXT else C.TRACK_TYPE_AUDIO

		var matchedIndex = -2
		val exoTracks = player.currentTracks
		for (groupInfo in exoTracks.groups) {
			if (matchedIndex > -2) break
			val trackType = groupInfo.type
			val group = groupInfo.mediaTrackGroup
			for (i in 0 until group.length) {
				if (trackType == chosenTrackType) {
					if (groupInfo.isTrackSelected(i)) {
						matchedIndex = -1
						val id: Int
						try {
							id =
								if (group.id.contains(":")) {
									group.id.split(":")[1].toInt()
								} else {
									group.id.toInt()
								}
						} catch (e: NumberFormatException) {
							Timber.w("failed to parse group ID [%s]", group.id)
							break
						}
						matchedIndex = id
						break
					}
				}
			}
		}

		val exoTrackID = offsetStreamIndex(matchedIndex, true, allStreams)
		if (exoTrackID < 0) return -1

		return exoTrackID
	}

	fun setExoPlayerTrack(
		index: Int,
		streamType: MediaStreamType?,
		allStreams: List<MediaStream>?,
	): Boolean {
		val player = mExoPlayer
		if (player == null ||
			allStreams.isNullOrEmpty() ||
			(streamType != MediaStreamType.SUBTITLE && streamType != MediaStreamType.AUDIO)
		) {
			return false
		}

		val chosenTrackType = if (streamType == MediaStreamType.SUBTITLE) C.TRACK_TYPE_TEXT else C.TRACK_TYPE_AUDIO

		val candidateOptional =
			allStreams.firstOrNull { stream ->
				stream.index == index && !stream.isExternal && stream.type == streamType
			}
		if (candidateOptional == null) return false

		val exoTrackID = offsetStreamIndex(index, false, allStreams)
		if (exoTrackID < 0) return false

		val exoTracks = player.currentTracks
		var matchedGroup: androidx.media3.common.TrackGroup? = null
		for (groupInfo in exoTracks.groups) {
			val trackType = groupInfo.type
			val group = groupInfo.mediaTrackGroup
			for (i in 0 until group.length) {
				val isSupported = groupInfo.isTrackSupported(i)
				val isSelected = groupInfo.isTrackSelected(i)
				val trackFormat = group.getFormat(i)

				Timber.i(
					"track %s group %s/%s trackType %s label %s mime %s isSelected %s isSupported %s",
					trackFormat.id,
					i + 1,
					group.length,
					trackType,
					trackFormat.label,
					trackFormat.sampleMimeType,
					isSelected,
					isSupported,
				)

				if (trackType != chosenTrackType) continue

				val id: Int
				try {
					id =
						if (group.id.contains(":")) {
							group.id.split(":")[1].toInt()
						} else {
							group.id.toInt()
						}
					if (id != exoTrackID) continue
				} catch (e: NumberFormatException) {
					Timber.w("failed to parse group ID [%s]", group.id)
					continue
				}

				if (!groupInfo.isTrackSupported(i)) {
					return false
				}

				if (groupInfo.isTrackSelected(i)) {
					return true
				}

				Timber.i("matched exoplayer track %s to mediaStream track %s", trackFormat.id, index)
				matchedGroup = group
			}
		}

		if (matchedGroup == null) return false

		return try {
			val selectionParams = player.trackSelectionParameters.buildUpon()
			selectionParams.setOverrideForType(TrackSelectionOverride(matchedGroup, 0))
			player.trackSelectionParameters = selectionParams.build()
			true
		} catch (e: Exception) {
			Timber.w("Error setting track selection")
			false
		}
	}

	var playbackSpeed: Float
		get() = mExoPlayer?.playbackParameters?.speed ?: 1.0f
		set(speed) {
			if (speed < 0.25f) {
				Timber.w("Invalid playback speed requested: %f", speed)
				return
			}
			Timber.d("Setting playback speed: %f", speed)
			mExoPlayer?.playbackParameters = PlaybackParameters(speed)
		}

	fun setSubtitleDelay(delayMs: Long) {
		if (!isInitialized()) {
			Timber.w("Cannot set subtitle delay: player not initialized")
			return
		}

		val handler = mSubtitleDelayHandler
		if (handler == null) {
			Timber.w("Cannot set subtitle delay: subtitle delay handler not initialized")
			return
		}

		Timber.d("Setting subtitle delay: %d ms", delayMs)
		handler.setOffsetMs(delayMs)
	}

	fun setAudioDelay(delayMs: Long) {
		Timber.d("Setting audio delay: %d ms", delayMs)
		mAudioDelayMs = delayMs

		mAudioDelayProcessor.setDelayMs(delayMs)

		val player = mExoPlayer
		if (player != null) {
			val currentPosition = player.currentPosition
			if (player.isPlaying || player.playbackState == Player.STATE_BUFFERING) {
				player.seekTo(currentPosition)
			}
		}
	}

	fun getAudioDelay(): Long = mAudioDelayMs

	fun destroy() {
		mPlaybackControllerNotifiable = null
		stopPlayback()
		releasePlayer()
	}

	private fun releasePlayer() {
		val player = mExoPlayer ?: return
		mSubtitleDelayHandler?.let { handler ->
			player.removeListener(handler)
			handler.release()
			mSubtitleDelayHandler = null
		}
		mExoPlayerView.player = null
		player.release()
		mExoPlayer = null
	}

	fun contractVideo(height: Int) {
		val lp = mExoPlayerView.layoutParams as FrameLayout.LayoutParams
		if (isContracted) return

		val sw = mActivity.window.decorView.width
		val sh = mActivity.window.decorView.height
		val ar = sw.toFloat() / sh
		lp.height = height
		lp.width = Math.ceil((height * ar).toDouble()).toInt()
		lp.rightMargin = ((lp.width - normalWidth) / 2) - 110
		lp.bottomMargin = ((lp.height - normalHeight) / 2) - 50

		mExoPlayerView.layoutParams = lp
		mExoPlayerView.invalidate()

		isContracted = true
	}

	fun setVideoFullSize(force: Boolean) {
		if (normalHeight == 0) return
		val lp = mExoPlayerView.layoutParams as FrameLayout.LayoutParams
		if (force) {
			lp.height = -1
			lp.width = -1
		} else {
			lp.height = normalHeight
			lp.width = normalWidth
		}

		lp.rightMargin = 0
		lp.bottomMargin = 0
		mExoPlayerView.layoutParams = lp
		mExoPlayerView.invalidate()

		isContracted = false
	}

	private var progressLoop: Runnable? = null

	private fun startProgressLoop() {
		stopProgressLoop()
		progressLoop =
			object : Runnable {
				override fun run() {
					mPlaybackControllerNotifiable?.onProgress()
					mHandler.postDelayed(this, 500)
				}
			}
		mHandler.post(progressLoop!!)
	}

	private fun stopProgressLoop() {
		progressLoop?.let { mHandler.removeCallbacks(it) }
	}
}
