package org.jellyfin.androidtv.ui.livetv

import androidx.fragment.app.Fragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jellyfin.androidtv.preference.SystemPreferences
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.sdk.model.api.BaseItemDto
import java.time.LocalDateTime
import java.util.UUID

object TvManager {
	lateinit var systemPreferences: SystemPreferences

	@Volatile
	private var allChannels: MutableList<BaseItemDto>? = null

	@Volatile
	private var channelIds: Array<UUID>? = null

	@Volatile
	private var forceReloadFlag = false

	// StateFlow mirrors for reactive observation (Compose / ViewModel)
	private val _channelsFlow = MutableStateFlow<List<BaseItemDto>>(emptyList())
	val channelsFlow: StateFlow<List<BaseItemDto>> = _channelsFlow.asStateFlow()

	private val _forceReloadFlow = MutableStateFlow(false)
	val forceReloadFlow: StateFlow<Boolean> = _forceReloadFlow.asStateFlow()

	@JvmStatic
	fun getLastLiveTvChannel(): UUID? =
		Utils.uuidOrNull(
			systemPreferences
				[SystemPreferences.liveTvLastChannel],
		)

	@JvmStatic
	fun setLastLiveTvChannel(id: UUID) {
		val systemPreferences = systemPreferences
		systemPreferences[SystemPreferences.liveTvPrevChannel] = systemPreferences[SystemPreferences.liveTvLastChannel]
		systemPreferences[SystemPreferences.liveTvLastChannel] = id.toString()
		updateLastPlayedDate(id)
		fillChannelIds()
	}

	@JvmStatic
	fun getPrevLiveTvChannel(): UUID? =
		Utils.uuidOrNull(
			systemPreferences
				[SystemPreferences.liveTvPrevChannel],
		)

	@JvmStatic
	fun getAllChannels(): List<BaseItemDto>? = allChannels

	fun setChannels(channels: List<BaseItemDto>) {
		allChannels = ArrayList(channels)
		_channelsFlow.value = channels
		fillChannelIds()
	}

	@JvmStatic
	fun forceReload() {
		forceReloadFlag = true
		_forceReloadFlow.value = true
	}

	@JvmStatic
	fun shouldForceReload(): Boolean = forceReloadFlag

	@JvmStatic
	fun getAllChannelsIndex(id: UUID): Int {
		val channels = allChannels ?: return -1
		return channels.indexOfFirst { it.id == id }
	}

	@JvmStatic
	fun getChannel(ndx: Int): BaseItemDto = allChannels!![ndx]

	@JvmStatic
	fun updateLastPlayedDate(channelId: UUID) {
		val channels = allChannels ?: return
		val ndx = getAllChannelsIndex(channelId)
		if (ndx >= 0) {
			channels[ndx] = channels[ndx].copyWithLastPlayedDate(LocalDateTime.now())
		}
	}

	@JvmStatic
	fun loadAllChannels(
		fragment: Fragment,
		outerResponse: java.util.function.Function<Int, Void?>,
	) {
		forceReloadFlag = false
		_forceReloadFlow.value = false
		loadLiveTvChannels(fragment) { channels ->
			if (channels != null) {
				allChannels = ArrayList(channels)
				_channelsFlow.value = channels.toList()
				outerResponse.apply(fillChannelIds())
			} else {
				outerResponse.apply(0)
			}
		}
	}

	private fun fillChannelIds(): Int {
		var ndx = 0
		val channels = allChannels
		if (channels != null) {
			val ids = Array(channels.size) { channels[it].id }
			channelIds = ids
			val last = getLastLiveTvChannel() ?: return ndx
			for (i in channels.indices) {
				if (channels[i].id == last) {
					ndx = i + 1
				}
			}
		}
		return ndx
	}
}
