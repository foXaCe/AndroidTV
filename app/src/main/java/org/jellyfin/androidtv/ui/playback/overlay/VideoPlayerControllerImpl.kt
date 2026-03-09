package org.jellyfin.androidtv.ui.playback.overlay

import org.jellyfin.androidtv.auth.repository.UserRepository
import org.jellyfin.androidtv.ui.playback.CustomPlaybackOverlayFragment
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.androidtv.util.apiclient.StreamHelper
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.koin.java.KoinJavaComponent

/**
 * Implements [VideoPlayerController] as a pure Kotlin class — no Leanback dependency.
 * Replaces the former VideoPlayerAdapter.java and the previous PlayerAdapter-based impl.
 */
class VideoPlayerControllerImpl(
	private val playbackController: PlaybackController,
	private var leanbackOverlayFragment: LeanbackOverlayFragment?,
) : VideoPlayerController {

	private var _masterOverlayFragment: CustomPlaybackOverlayFragment? = null

	// ── Transport ────────────────────────────────────────────────────

	override fun play() {
		playbackController.play(playbackController.currentPosition)
	}

	override fun pause() {
		playbackController.pause()
	}

	override fun rewind() {
		playbackController.rewind()
	}

	override fun fastForward() {
		playbackController.fastForward()
	}

	override fun seekTo(positionInMs: Long) {
		playbackController.seek(positionInMs)
	}

	override fun next() {
		playbackController.next()
	}

	override fun previous() {
		playbackController.prev()
	}

	override fun getDuration(): Long {
		val runTimeTicks = currentMediaSource?.runTimeTicks
			?: currentlyPlayingItem?.runTimeTicks
		return if (runTimeTicks != null) runTimeTicks / 10000 else -1
	}

	override fun getCurrentPosition(): Long = playbackController.currentPosition

	override fun isPlaying(): Boolean = playbackController.isPlaying

	override fun getBufferedPosition(): Long = playbackController.bufferedPosition

	// ── Media info queries ───────────────────────────────────────────

	override fun hasSubs(): Boolean =
		StreamHelper.getSubtitleStreams(playbackController.currentMediaSource).size > 0

	override fun hasMultiAudio(): Boolean =
		StreamHelper.getAudioStreams(playbackController.currentMediaSource).size > 1

	override fun hasNextItem(): Boolean = playbackController.hasNextItem()

	override fun hasPreviousItem(): Boolean = playbackController.hasPreviousItem()

	override fun canSeek(): Boolean = playbackController.canSeek()

	override fun isLiveTv(): Boolean = playbackController.isLiveTv

	override fun canRecordLiveTv(): Boolean {
		val item = currentlyPlayingItem ?: return false
		return item.currentProgram != null &&
			Utils.canManageRecordings(
				KoinJavaComponent.get<UserRepository>(UserRepository::class.java).currentUser.value
			)
	}

	override fun isRecording(): Boolean {
		val currentProgram = currentlyPlayingItem?.currentProgram ?: return false
		return currentProgram.timerId != null
	}

	override fun hasChapters(): Boolean {
		val chapters = currentlyPlayingItem?.chapters
		return chapters != null && chapters.isNotEmpty()
	}

	override fun hasCast(): Boolean {
		val item = currentlyPlayingItem ?: return false
		if (!item.people.isNullOrEmpty()) return true
		if (item.type == BaseItemKind.EPISODE && item.seriesId != null) return true
		return false
	}

	override val currentlyPlayingItem: BaseItemDto?
		get() = playbackController.currentlyPlayingItem

	override val currentMediaSource: MediaSourceInfo?
		get() = playbackController.currentMediaSource

	override fun toggleRecording() {
		val item = currentlyPlayingItem ?: return
		masterOverlayFragment.toggleRecording(item)
	}

	// ── Overlay control ──────────────────────────────────────────────

	override fun setOverlayFading(enabled: Boolean) {
		masterOverlayFragment.setFadingEnabled(enabled)
	}

	override fun hideOverlay() {
		leanbackOverlayFragment?.hideOverlay()
	}

	override val masterOverlayFragment: CustomPlaybackOverlayFragment
		get() = _masterOverlayFragment!!

	// ── Lifecycle ────────────────────────────────────────────────────

	fun setMasterOverlayFragment(fragment: CustomPlaybackOverlayFragment) {
		_masterOverlayFragment = fragment
	}

	fun detach() {
		_masterOverlayFragment = null
		leanbackOverlayFragment = null
	}
}
