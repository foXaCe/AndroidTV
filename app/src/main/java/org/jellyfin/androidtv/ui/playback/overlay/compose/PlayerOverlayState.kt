package org.jellyfin.androidtv.ui.playback.overlay.compose

import org.jellyfin.androidtv.preference.constant.ZoomMode
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemPerson
import org.jellyfin.sdk.model.api.ChapterInfo
import java.util.UUID

// Data classes for popup content
data class TrackOption(val index: Int, val title: String)
data class QualityOption(val key: String, val label: String)
data class SpeedOption(val speed: Float, val label: String)
data class ZoomOption(val mode: ZoomMode, val label: String)

data class PlayerOverlayState(
	val isVisible: Boolean = false,
	val isPlaying: Boolean = false,
	val position: Long = 0L,
	val duration: Long = 0L,
	val buffered: Long = 0L,
	val title: String = "",
	val subtitle: String = "",
	val endTime: String = "",

	// Capabilities
	val canSeek: Boolean = false,
	val isLiveTv: Boolean = false,
	val canRecordLiveTv: Boolean = false,
	val isRecording: Boolean = false,

	// Media tracks
	val hasMultiAudio: Boolean = false,
	val hasSubs: Boolean = false,
	val hasChapters: Boolean = false,
	val hasCast: Boolean = false,
	val hasNextItem: Boolean = false,
	val hasPreviousItem: Boolean = false,

	// Popup state
	val openPopup: PlayerPopup = PlayerPopup.None,
)

sealed class PlayerPopup {
	data object None : PlayerPopup()
	data class Audio(
		val tracks: List<TrackOption>,
		val selectedIndex: Int,
	) : PlayerPopup()

	data class Subtitles(
		val tracks: List<TrackOption>,
		val selectedIndex: Int,
	) : PlayerPopup()

	data class Quality(
		val profiles: List<QualityOption>,
		val selectedIndex: Int,
	) : PlayerPopup()

	data class Speed(
		val speeds: List<SpeedOption>,
		val selectedIndex: Int,
	) : PlayerPopup()

	data class Zoom(
		val modes: List<ZoomOption>,
		val selectedIndex: Int,
	) : PlayerPopup()

	data class SubtitleDelay(
		val currentDelayMs: Long,
	) : PlayerPopup()

	data class AudioDelay(
		val currentDelayMs: Long,
	) : PlayerPopup()

	data class Chapters(
		val itemId: UUID,
		val chapters: List<ChapterInfo>,
		val scrollToIndex: Int,
	) : PlayerPopup()

	data class Cast(
		val people: List<BaseItemPerson>,
	) : PlayerPopup()

	data class Channels(
		val channels: List<BaseItemDto>,
		val scrollToIndex: Int,
	) : PlayerPopup()

	data object Guide : PlayerPopup()
	data object Record : PlayerPopup()
	data object PreviousChannel : PlayerPopup()
}

sealed class PlayerOverlayAction {
	data object PlayPause : PlayerOverlayAction()
	data object Rewind : PlayerOverlayAction()
	data object FastForward : PlayerOverlayAction()
	data object SkipPrevious : PlayerOverlayAction()
	data object SkipNext : PlayerOverlayAction()
	data class SeekTo(val position: Long) : PlayerOverlayAction()

	// Popup toggles
	data object ShowAudio : PlayerOverlayAction()
	data object ShowSubtitles : PlayerOverlayAction()
	data object ShowQuality : PlayerOverlayAction()
	data object ShowSpeed : PlayerOverlayAction()
	data object ShowZoom : PlayerOverlayAction()
	data object ShowSubtitleDelay : PlayerOverlayAction()
	data object ShowAudioDelay : PlayerOverlayAction()
	data object ShowChapters : PlayerOverlayAction()
	data object ShowCast : PlayerOverlayAction()
	data object ShowChannels : PlayerOverlayAction()
	data object ShowGuide : PlayerOverlayAction()
	data object ToggleRecord : PlayerOverlayAction()
	data object PreviousChannel : PlayerOverlayAction()
	data object DismissPopup : PlayerOverlayAction()

	// Selection from popup
	data class SelectAudioTrack(val index: Int) : PlayerOverlayAction()
	data class SelectSubtitleTrack(val index: Int) : PlayerOverlayAction()
	data class SelectQuality(val bitrateKey: String) : PlayerOverlayAction()
	data class SelectSpeed(val speed: Float) : PlayerOverlayAction()
	data class SelectZoom(val mode: ZoomMode) : PlayerOverlayAction()
	data class SeekToChapter(val positionMs: Long) : PlayerOverlayAction()
	data class SwitchChannel(val channelId: UUID) : PlayerOverlayAction()
	data class OpenPerson(val personId: UUID?) : PlayerOverlayAction()

	// Delay stepper actions
	data class StepSubtitleDelay(val deltaMs: Long) : PlayerOverlayAction()
	data object ResetSubtitleDelay : PlayerOverlayAction()
	data class StepAudioDelay(val deltaMs: Long) : PlayerOverlayAction()
	data object ResetAudioDelay : PlayerOverlayAction()
}
