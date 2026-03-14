package org.jellyfin.androidtv.ui.playback

import android.os.Handler
import android.os.Looper
import androidx.media3.common.Player
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.SubtitleView
import timber.log.Timber
import java.util.LinkedList

private data class DelayedCue(
	val cues: List<Cue>,
	val showTimeMs: Long,
)

@UnstableApi
class SubtitleDelayHandler(
	private val subtitleView: SubtitleView,
) : Player.Listener {
	private val handler = Handler(Looper.getMainLooper())
	private val delayedCues = LinkedList<DelayedCue>()
	var offsetMs: Long = 0
		private set

	private var checkRunnable: Runnable? = null

	fun setOffsetMs(offsetMs: Long) {
		Timber.d("SubtitleDelayHandler: Setting offset to %d ms", offsetMs)
		this.offsetMs = offsetMs

		delayedCues.clear()
		checkRunnable?.let { handler.removeCallbacks(it) }
		checkRunnable = null

		subtitleView.setCues(emptyList())
	}

	override fun onCues(cueGroup: CueGroup) {
		if (offsetMs == 0L) {
			subtitleView.setCues(cueGroup.cues)
		} else if (offsetMs > 0) {
			val showTime = System.currentTimeMillis() + offsetMs
			delayedCues.offer(DelayedCue(cueGroup.cues, showTime))

			subtitleView.setCues(emptyList())

			scheduleCheck()
		} else {
			subtitleView.setCues(cueGroup.cues)
		}
	}

	private fun scheduleCheck() {
		if (checkRunnable != null) return

		val runnable =
			object : Runnable {
				override fun run() {
					checkRunnable = null
					val now = System.currentTimeMillis()

					while (delayedCues.isNotEmpty()) {
						val delayed = delayedCues.peek()
						if (delayed.showTimeMs <= now) {
							delayedCues.poll()
							subtitleView.setCues(delayed.cues)
						} else {
							val delay = delayed.showTimeMs - now
							handler.postDelayed(this, delay)
							checkRunnable = this
							break
						}
					}
				}
			}
		checkRunnable = runnable
		handler.post(runnable)
	}

	fun release() {
		checkRunnable?.let { handler.removeCallbacks(it) }
		checkRunnable = null
		delayedCues.clear()
		subtitleView.setCues(emptyList())
	}
}
