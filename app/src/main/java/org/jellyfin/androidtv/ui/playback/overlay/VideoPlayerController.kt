package org.jellyfin.androidtv.ui.playback.overlay

import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.MediaSourceInfo

/**
 * Pure Kotlin interface for player state and overlay control.
 * No Leanback dependency — used by all CustomAction subclasses.
 */
interface VideoPlayerController {
	// Transport
	fun play()

	fun pause()

	fun rewind()

	fun fastForward()

	fun seekTo(positionInMs: Long)

	fun next()

	fun previous()

	// State (functions to avoid JVM clash with PlayerAdapter methods)
	fun getDuration(): Long

	fun getCurrentPosition(): Long

	fun getBufferedPosition(): Long

	fun isPlaying(): Boolean

	// Media info queries
	fun hasSubs(): Boolean

	fun hasMultiAudio(): Boolean

	fun hasNextItem(): Boolean

	fun hasPreviousItem(): Boolean

	fun canSeek(): Boolean

	fun isLiveTv(): Boolean

	fun canRecordLiveTv(): Boolean

	fun isRecording(): Boolean

	fun hasChapters(): Boolean

	fun hasCast(): Boolean

	// Media data
	val currentlyPlayingItem: BaseItemDto?
	val currentMediaSource: MediaSourceInfo?

	// Recording
	fun toggleRecording()

	// Overlay control
	fun setOverlayFading(enabled: Boolean)

	fun hideOverlay()
}
