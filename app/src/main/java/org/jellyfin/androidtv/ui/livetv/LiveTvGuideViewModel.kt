package org.jellyfin.androidtv.ui.livetv

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.data.model.DataRefreshService
import org.jellyfin.androidtv.data.repository.ItemMutationRepository
import org.jellyfin.androidtv.preference.LiveTvPreferences
import org.jellyfin.androidtv.preference.SystemPreferences
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class LiveTvGuideUiState(
	val isLoading: Boolean = false,
	val channels: List<BaseItemDto> = emptyList(),
	val filteredChannels: List<BaseItemDto> = emptyList(),
	val programs: Map<UUID, List<BaseItemDto>> = emptyMap(),
	val selectedChannel: BaseItemDto? = null,
	val selectedProgram: BaseItemDto? = null,
	val guideStart: LocalDateTime = LocalDateTime.now(),
	val guideEnd: LocalDateTime = LocalDateTime.now(),
	val guideHours: Int = NORMAL_HOURS,
	val filters: GuideFilters,
	val error: String? = null,
) {
	companion object {
		const val NORMAL_HOURS = 9
		const val FILTERED_HOURS = 4
	}
}

class LiveTvGuideViewModel(
	private val context: Context,
	private val api: ApiClient,
	private val liveTvPreferences: LiveTvPreferences,
	private val itemMutationRepository: ItemMutationRepository,
	private val dataRefreshService: DataRefreshService,
	private val systemPreferences: SystemPreferences,
) : ViewModel() {
	private val _uiState = MutableStateFlow(LiveTvGuideUiState(filters = GuideFilters(context, systemPreferences)))
	val uiState: StateFlow<LiveTvGuideUiState> = _uiState.asStateFlow()

	private var hasLoaded = false

	fun loadGuide() {
		if (hasLoaded && !TvManager.shouldForceReload()) return
		hasLoaded = true

		val filters = _uiState.value.filters
		filters.load()

		val guideHours = if (filters.any()) LiveTvGuideUiState.FILTERED_HOURS else LiveTvGuideUiState.NORMAL_HOURS
		val start =
			LocalDateTime
				.now()
				.withSecond(0)
				.withNano(0)
		val end = start.plusHours(guideHours.toLong())

		_uiState.update { it.copy(guideStart = start, guideEnd = end, guideHours = guideHours, filters = filters) }

		loadChannelsAndPrograms(start, end)
	}

	fun forceReload() {
		TvManager.forceReload()
		hasLoaded = false
		_uiState.update { it.copy(programs = emptyMap()) }
		loadGuide()
	}

	fun pageGuideTo(startTime: LocalDateTime) {
		var adjustedStart = startTime
		if (adjustedStart.isBefore(LocalDateTime.now())) adjustedStart = LocalDateTime.now()

		val start = adjustedStart.withSecond(0).withNano(0)
		val filters = _uiState.value.filters
		val guideHours = if (filters.any()) LiveTvGuideUiState.FILTERED_HOURS else LiveTvGuideUiState.NORMAL_HOURS
		val end = start.plusHours(guideHours.toLong())

		_uiState.update { it.copy(guideStart = start, guideEnd = end, guideHours = guideHours, programs = emptyMap()) }
		TvManager.forceReload()
		hasLoaded = false
		loadChannelsAndPrograms(start, end)
	}

	private fun loadChannelsAndPrograms(
		start: LocalDateTime,
		end: LocalDateTime,
	) {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			try {
				val channels = loadLiveTvChannelsSuspend(api, liveTvPreferences)
				TvManager.setChannels(channels)

				val channelIds = channels.map { it.id }
				val programs = getProgramsSuspend(api, channelIds, start, end)
				val programsByChannel = buildProgramsMap(programs, start)

				val filters = _uiState.value.filters
				_uiState.update { state ->
					state.copy(
						isLoading = false,
						channels = channels,
						programs = programsByChannel,
						filteredChannels = applyFilters(channels, filters, programsByChannel),
						guideStart = start,
						guideEnd = end,
					)
				}
			} catch (e: Exception) {
				Timber.e(e, "Failed to load live TV guide")
				_uiState.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	fun selectChannel(channel: BaseItemDto) {
		_uiState.update { it.copy(selectedChannel = channel) }
	}

	fun selectProgram(program: BaseItemDto) {
		_uiState.update { it.copy(selectedProgram = program) }
	}

	fun toggleFavorite(channel: BaseItemDto) {
		viewModelScope.launch {
			try {
				val userData =
					itemMutationRepository.setFavorite(
						item = channel.id,
						favorite = !(channel.userData?.isFavorite ?: false),
					)

				val updatedChannel = channel.copy(userData = userData)
				dataRefreshService.lastFavoriteUpdate = Instant.now()

				_uiState.update { state ->
					val updatedChannels =
						state.channels.map {
							if (it.id == channel.id) updatedChannel else it
						}
					state.copy(
						channels = updatedChannels,
						filteredChannels = applyFilters(updatedChannels, state.filters, state.programs),
					)
				}
			} catch (e: Exception) {
				Timber.e(e, "Failed to toggle favorite")
			}
		}
	}

	fun refreshFavorite(channelId: UUID) {
		_uiState.update { state ->
			// Trigger recomposition by creating a new list
			state.copy(channels = state.channels.toList())
		}
	}

	fun updateProgram(updatedProgram: BaseItemDto) {
		_uiState.update { state ->
			val channelId = updatedProgram.channelId ?: return@update state
			val currentPrograms = state.programs.toMutableMap()
			val channelPrograms =
				currentPrograms[channelId]?.map {
					if (it.id == updatedProgram.id) updatedProgram else it
				}
			if (channelPrograms != null) {
				currentPrograms[channelId] = channelPrograms
			}
			state.copy(programs = currentPrograms)
		}
	}

	fun updateFilters(filters: GuideFilters) {
		_uiState.update { state ->
			state.copy(
				filters = filters,
				filteredChannels = applyFilters(state.channels, filters, state.programs),
			)
		}
	}

	fun clearError() {
		_uiState.update { it.copy(error = null) }
	}

	private fun applyFilters(
		channels: List<BaseItemDto>,
		filters: GuideFilters,
		programs: Map<UUID, List<BaseItemDto>>,
	): List<BaseItemDto> {
		if (!filters.any()) return channels
		return channels.filter { channel ->
			val channelPrograms = programs[channel.id] ?: return@filter false
			channelPrograms.any { filters.passesFilter(it) }
		}
	}

	private fun buildProgramsMap(
		programs: List<BaseItemDto>,
		startTime: LocalDateTime,
	): Map<UUID, List<BaseItemDto>> =
		programs
			.filter { it.channelId != null && it.endDate?.isAfter(startTime) == true }
			.groupBy { it.channelId!! }
}
