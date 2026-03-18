package org.jellyfin.androidtv.ui.player.video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.debug.ScreenIdOverlay
import org.jellyfin.androidtv.ui.base.debug.ScreenIds
import org.jellyfin.androidtv.ui.playback.VideoQueueManager
import org.jellyfin.androidtv.ui.playback.rewrite.RewriteMediaManager
import org.jellyfin.playback.core.PlaybackManager
import org.jellyfin.playback.core.model.PlayState
import org.jellyfin.playback.core.queue.queue
import org.jellyfin.sdk.api.client.ApiClient
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

class VideoPlayerFragment : Fragment() {
	data class Args(
		val position: Int? = null,
	) {
		fun toBundle() = bundleOf(EXTRA_POSITION to position)

		companion object {
			fun fromBundle(bundle: Bundle?): Args =
				Args(
					position = if (bundle?.containsKey(EXTRA_POSITION) == true) bundle.getInt(EXTRA_POSITION) else null,
				)
		}
	}

	companion object {
		internal const val EXTRA_POSITION: String = "position"
	}

	private val videoQueueManager by inject<VideoQueueManager>()
	private val playbackManager by inject<PlaybackManager>()
	private val api by inject<ApiClient>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Create a queue from the items added to the legacy video queue
		val queueSupplier = RewriteMediaManager.BaseItemQueueSupplier(api, videoQueueManager.getCurrentVideoQueue(), false)
		if (queueSupplier.items.isEmpty()) {
			Timber.e("Queue is EMPTY — nothing will play! Check VideoQueueManager state.")
		} else {
			Timber.i("Created a queue with ${queueSupplier.items.size} items")
		}
		playbackManager.queue.clear()
		playbackManager.queue.addSupplier(queueSupplier)

		// Set position — wait for PLAYING state (not just PAUSED/BUFFERING) then seek
		Args.fromBundle(arguments).position?.milliseconds?.let { seekPosition ->
			lifecycleScope.launch {
				try {
					val readyState =
						withTimeout(5000) {
							playbackManager.state.playState.first {
								it == PlayState.PLAYING || it == PlayState.ERROR
							}
						}
					if (readyState == PlayState.ERROR) {
						Timber.e("Player entered ERROR state, skipping initial seek to $seekPosition")
					} else {
						Timber.i("Player is PLAYING, seeking to $seekPosition")
						playbackManager.state.seek(seekPosition)
					}
				} catch (_: TimeoutCancellationException) {
					Timber.w("Player not PLAYING after 5s, skipping initial seek to $seekPosition")
				}
			}
		}

		// Pause player until the initial resume
		playbackManager.state.pause()
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	) = content {
		JellyfinTheme {
			ScreenIdOverlay(ScreenIds.VIDEO_PLAYER_ID, ScreenIds.VIDEO_PLAYER_NAME) {
				VideoPlayerScreen()
			}
		}
	}

	override fun onPause() {
		super.onPause()

		playbackManager.state.pause()
	}

	override fun onResume() {
		super.onResume()

		playbackManager.state.unpause()
	}

	override fun onStop() {
		super.onStop()

		playbackManager.state.stop()
	}
}
