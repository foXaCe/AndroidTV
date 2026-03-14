package org.jellyfin.androidtv.ui.home.mediabar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.VideoStream
import timber.log.Timber
import java.util.Locale

/**
 * Resolves direct video stream URLs from YouTube video IDs using
 * NewPipe Extractor, which properly handles YouTube's n-parameter
 * descrambling to avoid CDN throttling / HTTP 403 errors.
 *
 * This resolver picks:
 *  1. The best H.264 (avc1) video-only stream ≥ 1080p (skips trailers below 1080p)
 *  2. Falls back to VP9 or AV1 if no avc1 is available
 *  3. The best AAC (mp4a) audio stream for the audio track
 */
object YouTubeStreamResolver {
	private const val TAG = "YouTubeStream"

	@Volatile
	private var initialized = false

	data class StreamInfo(
		val videoUrl: String,
		val audioUrl: String?,
		val isVideoOnly: Boolean,
	)

	@Synchronized
	private fun ensureInitialized() {
		if (!initialized) {
			NewPipe.init(NewPipeDownloader.getInstance())
			initialized = true
		}
	}

	suspend fun resolveStream(videoId: String): StreamInfo? =
		withContext(Dispatchers.IO) {
			try {
				ensureInitialized()
				val result = extractStreams(videoId)
				if (result == null) {
					Timber.w("$TAG: NewPipe Extractor returned no usable streams for $videoId")
				}
				result
			} catch (e: Throwable) {
				Timber.w(e, "$TAG: NewPipe Extractor failed for $videoId")
				null
			}
		}

	private fun extractStreams(videoId: String): StreamInfo? {
		val url = "https://www.youtube.com/watch?v=$videoId"
		val extractor = ServiceList.YouTube.getStreamExtractor(url)
		extractor.fetchPage()

		val videoOnlyStreams =
			extractor.videoOnlyStreams
				.orEmpty()
				.filter { it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP && it.content.isNotBlank() }

		val muxedStreams =
			extractor.videoStreams
				.orEmpty()
				.filter { it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP && it.content.isNotBlank() }

		val audioStreams =
			extractor.audioStreams
				.orEmpty()
				.filter { it.deliveryMethod == DeliveryMethod.PROGRESSIVE_HTTP && it.content.isNotBlank() }

		val bestVideo = pickBestVideo(videoOnlyStreams)
		if (bestVideo != null) {
			val bestAudio = pickBestAudio(audioStreams)
			return StreamInfo(
				videoUrl = bestVideo.content,
				audioUrl = bestAudio?.content,
				isVideoOnly = true,
			)
		}

		val bestMuxed = pickBestVideo(muxedStreams)
		if (bestMuxed != null) {
			return StreamInfo(
				videoUrl = bestMuxed.content,
				audioUrl = null,
				isVideoOnly = false,
			)
		}

		return null
	}

	private fun pickBestVideo(streams: List<VideoStream>): VideoStream? {
		// Only consider streams that are at least 1080p
		val hdStreams = streams.filter { it.height >= 1080 }
		if (hdStreams.isEmpty()) {
			Timber.d("$TAG: No streams >= 1080p available, skipping trailer")
			return null
		}

		// Pick the best 1080p stream (prefer H.264, then closest to 1080p)
		return hdStreams
			.sortedWith(compareBy<VideoStream> { codecPriority(it.codec) }.thenBy { it.height })
			.firstOrNull()
	}

	private fun pickBestAudio(streams: List<AudioStream>): AudioStream? {
		val deviceLocale = Locale.getDefault()
		return streams
			.sortedWith(
				compareBy<AudioStream> {
					// Prefer French audio, then device locale, then others
					val lang = it.audioLocale?.language
					when {
						lang == "fr" -> 0
						lang == deviceLocale.language -> 1
						else -> 2
					}
				}.thenBy {
					if (it.codec?.startsWith("mp4a") == true) 0 else 1
				}.thenByDescending { it.averageBitrate },
			).firstOrNull()
	}

	private fun codecPriority(codec: String?): Int =
		when {
			codec == null -> 4
			codec.startsWith("avc1") -> 0
			codec.startsWith("vp09") || codec.startsWith("vp9") -> 1
			codec.startsWith("av01") -> 2
			else -> 3
		}
}
