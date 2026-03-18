package org.jellyfin.playback.jellyfin.mediastream

import org.jellyfin.playback.core.mediastream.MediaConversionMethod
import org.jellyfin.playback.core.mediastream.MediaStreamResolver
import org.jellyfin.playback.core.mediastream.PlayableMediaStream
import org.jellyfin.playback.core.queue.QueueEntry
import org.jellyfin.playback.jellyfin.queue.baseItem
import org.jellyfin.playback.jellyfin.queue.mediaSourceId
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.audioApi
import org.jellyfin.sdk.api.client.extensions.mediaInfoApi
import org.jellyfin.sdk.api.client.extensions.videosApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.DeviceProfile
import org.jellyfin.sdk.model.api.MediaProtocol
import org.jellyfin.sdk.model.api.MediaType
import org.jellyfin.sdk.model.api.PlaybackInfoDto
import timber.log.Timber

class JellyfinMediaStreamResolver(
	private val api: ApiClient,
	private val deviceProfileBuilder: () -> DeviceProfile,
) : MediaStreamResolver {
	companion object {
		private val supportedMediaTypes = arrayOf(MediaType.VIDEO, MediaType.AUDIO)
	}

	override suspend fun getStream(queueEntry: QueueEntry): PlayableMediaStream? {
		val t0 = System.currentTimeMillis()
		val baseItem = queueEntry.baseItem
		if (baseItem == null || !supportedMediaTypes.contains(baseItem.mediaType)) return null

		val mediaInfo = getPlaybackInfo(baseItem, queueEntry.mediaSourceId)
		val t1 = System.currentTimeMillis()
		Timber.tag("VFX_PERF").i("VFX_PERF MediaStreamResolver getPlaybackInfo: ${t1 - t0}ms (item=${baseItem.name})")

		// Force direct play for remote/strm sources — transcoding remote streams is unreliable
		val forceDirectPlay = mediaInfo.mediaSource.isRemote && mediaInfo.mediaSource.protocol == MediaProtocol.HTTP
		if (forceDirectPlay && !mediaInfo.mediaSource.supportsDirectPlay) {
			Timber.w("Remote source doesn't support direct play (server wants to transcode), forcing direct play anyway")
		}

		return when {
			// Direct play video (or forced for remote sources)
			(mediaInfo.mediaSource.supportsDirectPlay || forceDirectPlay) && baseItem.mediaType == MediaType.VIDEO ->
				mediaInfo.toStream(
					queueEntry = queueEntry,
					conversionMethod = MediaConversionMethod.None,
					url =
						api.videosApi.getVideoStreamUrl(
							itemId = baseItem.id,
							container = mediaInfo.mediaSource.container,
							mediaSourceId = mediaInfo.mediaSource.id,
							static = true,
							tag = mediaInfo.mediaSource.eTag,
							liveStreamId = mediaInfo.mediaSource.liveStreamId,
						),
				)

			// Direct play audio (or forced for remote sources)
			(mediaInfo.mediaSource.supportsDirectPlay || forceDirectPlay) && baseItem.mediaType == MediaType.AUDIO ->
				mediaInfo.toStream(
					queueEntry = queueEntry,
					conversionMethod = MediaConversionMethod.None,
					url =
						api.audioApi.getAudioStreamUrl(
							itemId = baseItem.id,
							container = mediaInfo.mediaSource.container,
							mediaSourceId = mediaInfo.mediaSource.id,
							static = true,
							tag = mediaInfo.mediaSource.eTag,
							liveStreamId = mediaInfo.mediaSource.liveStreamId,
						),
				)

			// Remux (direct stream)
			mediaInfo.mediaSource.supportsDirectStream && mediaInfo.mediaSource.transcodingUrl != null ->
				mediaInfo.toStream(
					queueEntry = queueEntry,
					conversionMethod = MediaConversionMethod.Remux,
					url = api.createUrl(requireNotNull(mediaInfo.mediaSource.transcodingUrl), ignorePathParameters = true),
				)

			// Transcode
			mediaInfo.mediaSource.supportsTranscoding && mediaInfo.mediaSource.transcodingUrl != null ->
				mediaInfo.toStream(
					queueEntry = queueEntry,
					conversionMethod = MediaConversionMethod.Transcode,
					url = api.createUrl(requireNotNull(mediaInfo.mediaSource.transcodingUrl), ignorePathParameters = true),
				)

			// No compatible stream found
			else -> null
		}
	}

	private suspend fun getPlaybackInfo(
		item: BaseItemDto,
		mediaSourceId: String? = null,
	): MediaInfo {
		val profile = deviceProfileBuilder()
		val response by api.mediaInfoApi.getPostedPlaybackInfo(
			itemId = item.id,
			data =
				PlaybackInfoDto(
					mediaSourceId = mediaSourceId,
					deviceProfile = profile,
					enableDirectPlay = true,
					enableDirectStream = true,
					enableTranscoding = true,
					allowVideoStreamCopy = true,
					allowAudioStreamCopy = true,
					autoOpenLiveStream = false,
				),
		)

		if (response.errorCode != null) {
			error("Failed to get media info for item ${item.id} source $mediaSourceId: ${response.errorCode}")
		}

		// Log all media sources for debugging
		response.mediaSources.forEachIndexed { i, src ->
			Timber.d(
				"MediaSource[$i]: id=${src.id}, protocol=${src.protocol}, isRemote=${src.isRemote}, container=${src.container}, supportsDirectPlay=${src.supportsDirectPlay}, supportsDirectStream=${src.supportsDirectStream}",
			)
		}

		val mediaSource =
			response.mediaSources
				// Select first valid media source (allow HTTP protocol for remote/strm files)
				.firstOrNull { mediaSourceId == null || it.id == mediaSourceId }

		requireNotNull(mediaSource) {
			"Failed to get media info for item ${item.id} source $mediaSourceId: media source missing in response (${response.mediaSources.size} sources returned)"
		}

		return MediaInfo(
			playSessionId = response.playSessionId.orEmpty(),
			mediaSource = mediaSource,
		)
	}

	/**
	 * Append the API key to a stream URL so ExoPlayer can authenticate without custom headers.
	 * The Jellyfin SDK's [ApiClient.createUrl] does not include the token in generated URLs.
	 */
	private fun appendApiKey(url: String): String {
		val token = api.accessToken ?: return url
		val separator = if ('?' in url) '&' else '?'
		return "${url}${separator}api_key=$token"
	}

	private fun MediaInfo.toStream(
		queueEntry: QueueEntry,
		conversionMethod: MediaConversionMethod,
		url: String,
	) = PlayableMediaStream(
		identifier = playSessionId,
		conversionMethod = conversionMethod,
		container = getMediaStreamContainer(),
		tracks = getTracks(),
		queueEntry = queueEntry,
		url = appendApiKey(url),
	)
}
