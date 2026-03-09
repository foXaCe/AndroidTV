package org.jellyfin.androidtv.ui.playback.overlay.compose

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.getQualityProfiles
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.androidtv.ui.playback.VideoSpeedController
import org.jellyfin.androidtv.ui.playback.overlay.VideoPlayerController
import org.jellyfin.androidtv.ui.playback.setSubtitleIndex
import org.jellyfin.androidtv.util.getTimeFormatter
import org.jellyfin.sdk.model.api.MediaStreamType
import timber.log.Timber
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToLong

class PlayerOverlayViewModel(
	private val context: Context,
	private val playerController: VideoPlayerController,
	private val playbackController: PlaybackController,
	private val userPreferences: UserPreferences,
) {
	private val _state = MutableStateFlow(PlayerOverlayState())
	val state: StateFlow<PlayerOverlayState> = _state.asStateFlow()

	private var pollingJob: Job? = null

	// Speed controller
	private val speedController = VideoSpeedController(playbackController)
	private val speeds = VideoSpeedController.SpeedSteps.entries.toTypedArray()

	// Quality profiles
	private val qualityProfiles = getQualityProfiles(context)
	private var currentQualityKey = userPreferences[UserPreferences.maxBitrate]

	// Delay tracking
	private var currentSubtitleDelayMs: Long = 0L
	private var currentAudioDelayMs: Long = 0L

	fun startCollecting(scope: CoroutineScope) {
		pollingJob?.cancel()
		pollingJob = scope.launch {
			while (isActive) {
				updateState()
				delay(POLL_INTERVAL_MS)
			}
		}
	}

	fun stopCollecting() {
		pollingJob?.cancel()
		pollingJob = null
	}

	private fun updateState() {
		val duration = playerController.getDuration()
		val position = playerController.getCurrentPosition()
		val isPlaying = playerController.isPlaying()

		_state.update { current ->
			current.copy(
				isPlaying = isPlaying,
				position = position,
				duration = duration,
				buffered = playerController.getBufferedPosition(),
				endTime = computeEndTime(duration, position, isPlaying),
				canSeek = playerController.canSeek(),
				isLiveTv = playerController.isLiveTv(),
				canRecordLiveTv = playerController.canRecordLiveTv(),
				isRecording = playerController.isRecording(),
				hasMultiAudio = playerController.hasMultiAudio(),
				hasSubs = playerController.hasSubs(),
				hasChapters = playerController.hasChapters(),
				hasCast = playerController.hasCast(),
				hasNextItem = playerController.hasNextItem(),
				hasPreviousItem = playerController.hasPreviousItem(),
			)
		}
	}

	fun updateMediaInfo() {
		val item = playerController.currentlyPlayingItem
		_state.update { current ->
			current.copy(
				title = item?.name.orEmpty(),
				subtitle = buildSubtitle(item),
			)
		}
	}

	private fun buildSubtitle(item: org.jellyfin.sdk.model.api.BaseItemDto?): String {
		if (item == null) return ""
		return when (item.type) {
			org.jellyfin.sdk.model.api.BaseItemKind.EPISODE -> {
				val series = item.seriesName.orEmpty()
				val season = item.parentIndexNumber
				val episode = item.indexNumber
				if (season != null && episode != null) "$series S${season}E${episode}"
				else series
			}
			else -> ""
		}
	}

	private fun computeEndTime(duration: Long, position: Long, isPlaying: Boolean): String {
		if (duration < 1 || !isPlaying) return ""
		val msLeft = duration - position
		val realTimeLeft = (msLeft / playbackController.playbackSpeed).roundToLong()
		val endTime = LocalDateTime.now().plus(realTimeLeft, ChronoUnit.MILLIS)
		return context.getString(
			R.string.lbl_playback_control_ends,
			context.getTimeFormatter().format(endTime)
		)
	}

	// -- Overlay visibility --

	fun showOverlay() {
		_state.update { it.copy(isVisible = true) }
	}

	fun hideOverlay() {
		_state.update { it.copy(isVisible = false, openPopup = PlayerPopup.None) }
	}

	fun setVisible(visible: Boolean) {
		if (visible) showOverlay() else hideOverlay()
	}

	// -- Actions --

	fun onAction(action: PlayerOverlayAction) {
		when (action) {
			is PlayerOverlayAction.PlayPause -> {
				if (playerController.isPlaying()) playerController.pause()
				else playerController.play()
			}
			is PlayerOverlayAction.Rewind -> playerController.rewind()
			is PlayerOverlayAction.FastForward -> playerController.fastForward()
			is PlayerOverlayAction.SkipPrevious -> playerController.previous()
			is PlayerOverlayAction.SkipNext -> playerController.next()
			is PlayerOverlayAction.SeekTo -> playerController.seekTo(action.position)

			is PlayerOverlayAction.ShowAudio -> showAudioPopup()
			is PlayerOverlayAction.ShowSubtitles -> showSubtitlesPopup()
			is PlayerOverlayAction.ShowQuality -> showQualityPopup()
			is PlayerOverlayAction.ShowSpeed -> showSpeedPopup()
			is PlayerOverlayAction.ShowZoom -> showZoomPopup()
			is PlayerOverlayAction.ShowSubtitleDelay -> showSubtitleDelayPopup()
			is PlayerOverlayAction.ShowAudioDelay -> showAudioDelayPopup()
			is PlayerOverlayAction.ShowChapters -> {
				val item = playerController.currentlyPlayingItem
				val chapters = item?.chapters.orEmpty()
				val currentPos = playerController.getCurrentPosition()
				val scrollIndex = chapters.indexOfLast {
					it.startPositionTicks / 10_000 <= currentPos
				}.coerceAtLeast(0)
				showPopup(PlayerPopup.Chapters(
					itemId = item?.id ?: return,
					chapters = chapters,
					scrollToIndex = scrollIndex,
				))
			}
			is PlayerOverlayAction.ShowCast -> {
				val people = playerController.currentlyPlayingItem?.people.orEmpty()
				showPopup(PlayerPopup.Cast(people))
			}
			is PlayerOverlayAction.ShowChannels -> {
				// Delegated to the master overlay fragment
			}
			is PlayerOverlayAction.ShowGuide -> {
				// Delegated to the master overlay fragment
			}
			is PlayerOverlayAction.ToggleRecord -> playerController.toggleRecording()
			is PlayerOverlayAction.PreviousChannel -> {
				// Delegated to the master overlay fragment
			}
			is PlayerOverlayAction.DismissPopup -> dismissPopup()

			is PlayerOverlayAction.SelectAudioTrack -> {
				playbackController.switchAudioStream(action.index)
				dismissPopup()
			}
			is PlayerOverlayAction.SelectSubtitleTrack -> {
				playbackController.setSubtitleIndex(action.index)
				dismissPopup()
			}
			is PlayerOverlayAction.SelectQuality -> {
				currentQualityKey = action.bitrateKey
				userPreferences[UserPreferences.maxBitrate] = action.bitrateKey
				playbackController.refreshStream()
				dismissPopup()
			}
			is PlayerOverlayAction.SelectSpeed -> {
				val step = speeds.firstOrNull { it.speed == action.speed }
				if (step != null) speedController.currentSpeed = step
				dismissPopup()
			}
			is PlayerOverlayAction.SelectZoom -> {
				playbackController.setZoom(action.mode)
				dismissPopup()
			}
			is PlayerOverlayAction.SeekToChapter -> {
				playbackController.seek(action.positionMs)
				dismissPopup()
			}
			is PlayerOverlayAction.SwitchChannel -> {
				// Delegated to the master overlay fragment
			}
			is PlayerOverlayAction.OpenPerson -> {
				// Delegated to the master overlay fragment
			}

			// Delay stepper actions
			is PlayerOverlayAction.StepSubtitleDelay -> {
				val popup = _state.value.openPopup as? PlayerPopup.SubtitleDelay ?: return
				val newDelay = (popup.currentDelayMs + action.deltaMs).coerceIn(
					-MAX_DELAY_MS, MAX_DELAY_MS
				)
				applySubtitleDelay(newDelay)
				_state.update { it.copy(openPopup = PlayerPopup.SubtitleDelay(newDelay)) }
			}
			is PlayerOverlayAction.ResetSubtitleDelay -> {
				applySubtitleDelay(0L)
				_state.update { it.copy(openPopup = PlayerPopup.SubtitleDelay(0L)) }
			}
			is PlayerOverlayAction.StepAudioDelay -> {
				val popup = _state.value.openPopup as? PlayerPopup.AudioDelay ?: return
				val newDelay = (popup.currentDelayMs + action.deltaMs).coerceIn(
					-MAX_DELAY_MS, MAX_DELAY_MS
				)
				applyAudioDelay(newDelay)
				_state.update { it.copy(openPopup = PlayerPopup.AudioDelay(newDelay)) }
			}
			is PlayerOverlayAction.ResetAudioDelay -> {
				applyAudioDelay(0L)
				_state.update { it.copy(openPopup = PlayerPopup.AudioDelay(0L)) }
			}
		}
	}

	// -- Popup builders --

	private fun showAudioPopup() {
		val audioTracks = playbackController.currentStreamInfo?.selectableAudioStreams.orEmpty()
		val currentIndex = playbackController.audioStreamIndex
		val tracks = audioTracks.map { stream ->
			TrackOption(stream.index, stream.displayTitle ?: "Track ${stream.index}")
		}
		val selectedIdx = tracks.indexOfFirst { it.index == currentIndex }
		showPopup(PlayerPopup.Audio(tracks, selectedIdx.coerceAtLeast(0)))
	}

	private fun showSubtitlesPopup() {
		if (playbackController.currentStreamInfo == null) {
			Timber.w("StreamInfo null trying to obtain subtitles")
			return
		}
		val mediaStreams = playbackController.currentMediaSource?.mediaStreams.orEmpty()
		val subtitleStreams = mediaStreams.filter { it.type == MediaStreamType.SUBTITLE }
		val currentSubIndex = playbackController.subtitleStreamIndex

		val tracks = mutableListOf(
			TrackOption(-1, context.getString(R.string.lbl_none))
		)
		tracks.addAll(subtitleStreams.map { stream ->
			TrackOption(stream.index, stream.displayTitle ?: "Track ${stream.index}")
		})
		val selectedIdx = tracks.indexOfFirst { it.index == currentSubIndex }
		showPopup(PlayerPopup.Subtitles(tracks, selectedIdx.coerceAtLeast(0)))
	}

	private fun showQualityPopup() {
		val profiles = qualityProfiles.map { (key, label) ->
			QualityOption(key, label)
		}
		val selectedIdx = profiles.indexOfFirst { it.key == currentQualityKey }
		showPopup(PlayerPopup.Quality(profiles, selectedIdx.coerceAtLeast(0)))
	}

	private fun showSpeedPopup() {
		val speedOptions = speeds.map { step ->
			SpeedOption(step.speed, String.format(Locale.US, "%.2fx", step.speed))
		}
		val selectedIdx = speeds.indexOf(speedController.currentSpeed)
		showPopup(PlayerPopup.Speed(speedOptions, selectedIdx.coerceAtLeast(0)))
	}

	private fun showZoomPopup() {
		val zoomModes = ZoomMode.entries.map { mode ->
			ZoomOption(mode, context.getString(mode.nameRes))
		}
		val currentZoom = playbackController.zoomMode
		val selectedIdx = ZoomMode.entries.indexOf(currentZoom)
		showPopup(PlayerPopup.Zoom(zoomModes, selectedIdx.coerceAtLeast(0)))
	}

	private fun showSubtitleDelayPopup() {
		showPopup(PlayerPopup.SubtitleDelay(currentSubtitleDelayMs))
	}

	private fun showAudioDelayPopup() {
		showPopup(PlayerPopup.AudioDelay(currentAudioDelayMs))
	}

	// -- Delay application --

	private fun applySubtitleDelay(delayMs: Long) {
		if (playbackController.isBurningSubtitles) {
			Timber.w("Subtitles are burned in, delay not applicable")
			return
		}
		val videoManager = playbackController.videoManager ?: return
		videoManager.setSubtitleDelay(delayMs)
		currentSubtitleDelayMs = delayMs
	}

	private fun applyAudioDelay(delayMs: Long) {
		playbackController.setAudioDelay(delayMs)
		currentAudioDelayMs = delayMs
	}

	// -- Popup state management --

	private fun showPopup(popup: PlayerPopup) {
		playerController.setOverlayFading(false)
		_state.update { it.copy(openPopup = popup) }
	}

	private fun dismissPopup() {
		playerController.setOverlayFading(true)
		_state.update { it.copy(openPopup = PlayerPopup.None) }
	}

	companion object {
		private const val POLL_INTERVAL_MS = 500L
		private const val MAX_DELAY_MS = 10_000L
	}
}
