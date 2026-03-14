package org.jellyfin.androidtv.ui.livetv.compose

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jellyfin.androidtv.data.repository.ItemMutationRepository
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.liveTvApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.SeriesTimerInfoDto
import org.jellyfin.sdk.model.api.TimerInfoDto
import java.util.UUID

// ── DTO copy helpers ───────────────────────────────────────────────

fun BaseItemDto.copyWithTimerId(timerId: String?) = copy(timerId = timerId)

fun BaseItemDto.copyWithSeriesTimerId(seriesTimerId: String?) = copy(seriesTimerId = seriesTimerId)

fun SeriesTimerInfoDto.copyWithPrePaddingSeconds(seconds: Int) = copy(prePaddingSeconds = seconds)

fun SeriesTimerInfoDto.copyWithPostPaddingSeconds(seconds: Int) = copy(postPaddingSeconds = seconds)

fun SeriesTimerInfoDto.copyWithFilters(
	recordNewOnly: Boolean,
	recordAnyChannel: Boolean,
	recordAnyTime: Boolean,
) = copy(
	recordNewOnly = recordNewOnly,
	recordAnyChannel = recordAnyChannel,
	recordAnyTime = recordAnyTime,
)

fun SeriesTimerInfoDto.asTimerInfoDto() =
	TimerInfoDto(
		id = id,
		type = type,
		serverId = serverId,
		externalId = externalId,
		channelId = channelId,
		externalChannelId = externalChannelId,
		channelName = channelName,
		channelPrimaryImageTag = channelPrimaryImageTag,
		programId = programId,
		externalProgramId = externalProgramId,
		name = name,
		overview = overview,
		startDate = startDate,
		endDate = endDate,
		serviceName = serviceName,
		priority = priority,
		prePaddingSeconds = prePaddingSeconds,
		postPaddingSeconds = postPaddingSeconds,
		isPrePaddingRequired = isPrePaddingRequired,
		parentBackdropItemId = parentBackdropItemId,
		parentBackdropImageTags = parentBackdropImageTags,
		isPostPaddingRequired = isPostPaddingRequired,
		keepUntil = keepUntil,
	)

fun createProgramTimerInfo(
	programId: UUID,
	options: SeriesTimerInfoDto,
) = TimerInfoDto(
	programId = programId.toString(),
	prePaddingSeconds = options.prePaddingSeconds,
	postPaddingSeconds = options.postPaddingSeconds,
	isPrePaddingRequired = options.isPrePaddingRequired,
	isPostPaddingRequired = options.isPostPaddingRequired,
)

// ── Suspend API functions ──────────────────────────────────────────

suspend fun cancelLiveTvTimer(
	api: ApiClient,
	timerId: String,
) {
	withContext(Dispatchers.IO) { api.liveTvApi.cancelTimer(timerId) }
}

suspend fun cancelLiveTvSeriesTimer(
	api: ApiClient,
	seriesTimerId: String,
) {
	withContext(Dispatchers.IO) { api.liveTvApi.cancelSeriesTimer(seriesTimerId) }
}

suspend fun recordLiveTvProgram(
	api: ApiClient,
	programId: UUID,
): BaseItemDto =
	withContext(Dispatchers.IO) {
		val seriesTimer by api.liveTvApi.getDefaultTimer(programId.toString())
		val timer = seriesTimer.asTimerInfoDto()
		api.liveTvApi.createTimer(timer)
		api.liveTvApi.getProgram(programId.toString()).content
	}

suspend fun recordLiveTvSeries(
	api: ApiClient,
	programId: UUID,
): BaseItemDto =
	withContext(Dispatchers.IO) {
		val timer by api.liveTvApi.getDefaultTimer(programId.toString())
		api.liveTvApi.createSeriesTimer(timer)
		api.liveTvApi.getProgram(programId.toString()).content
	}

suspend fun getLiveTvSeriesTimer(
	api: ApiClient,
	seriesTimerId: String,
): SeriesTimerInfoDto =
	withContext(Dispatchers.IO) {
		api.liveTvApi.getSeriesTimer(seriesTimerId).content
	}

suspend fun updateLiveTvSeriesTimer(
	api: ApiClient,
	seriesTimer: SeriesTimerInfoDto,
) {
	withContext(Dispatchers.IO) {
		val id = seriesTimer.id
		if (id == null) {
			api.liveTvApi.createSeriesTimer(seriesTimer)
		} else {
			api.liveTvApi.updateSeriesTimer(id, seriesTimer)
		}
	}
}

suspend fun updateLiveTvTimer(
	api: ApiClient,
	timer: TimerInfoDto,
) {
	withContext(Dispatchers.IO) {
		val id = timer.id
		if (id == null) {
			api.liveTvApi.createTimer(timer)
		} else {
			api.liveTvApi.updateTimer(id, timer)
		}
	}
}

suspend fun getLiveTvProgram(
	api: ApiClient,
	id: UUID,
): BaseItemDto =
	withContext(Dispatchers.IO) {
		api.liveTvApi.getProgram(id.toString()).content
	}

suspend fun toggleLiveTvFavorite(
	itemMutationRepository: ItemMutationRepository,
	item: BaseItemDto,
): BaseItemDto {
	val userData =
		itemMutationRepository.setFavorite(
			item = item.id,
			favorite = !(item.userData?.isFavorite ?: false),
		)
	return item.copy(userData = userData)
}
