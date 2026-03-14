package org.jellyfin.androidtv.data.compat

import org.jellyfin.sdk.model.api.MediaSourceInfo
import org.jellyfin.sdk.model.api.MediaStream
import org.jellyfin.sdk.model.api.MediaStreamType
import org.jellyfin.sdk.model.api.PlayMethod
import org.jellyfin.sdk.model.api.SubtitleDeliveryMethod
import java.util.UUID

class StreamInfo {
	var itemId: UUID? = null
	var mediaUrl: String? = null
	var playMethod: PlayMethod = PlayMethod.DIRECT_PLAY
	var container: String? = null
	var runTimeTicks: Long? = null
	var mediaSource: MediaSourceInfo? = null
	var playSessionId: String? = null

	val subtitleDeliveryMethod: SubtitleDeliveryMethod
		get() {
			val index =
				mediaSource?.defaultSubtitleStreamIndex
					?: return SubtitleDeliveryMethod.DROP
			if (index == -1) return SubtitleDeliveryMethod.DROP
			return mediaSource?.mediaStreams?.get(index)?.deliveryMethod
				?: SubtitleDeliveryMethod.DROP
		}

	val mediaSourceId: String?
		get() = mediaSource?.id

	val selectableAudioStreams: List<MediaStream>
		get() = getSelectableStreams(MediaStreamType.AUDIO)

	fun getSelectableStreams(type: MediaStreamType): List<MediaStream> = mediaSource?.mediaStreams?.filter { it.type == type }.orEmpty()
}
