package org.jellyfin.androidtv.ui.itemhandling

import android.content.Context
import org.jellyfin.androidtv.auth.repository.SessionRepository
import org.jellyfin.androidtv.constant.LiveTvOption
import org.jellyfin.androidtv.ui.navigation.Destination
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.playback.PlaybackLauncher
import org.jellyfin.androidtv.util.PlaybackHelper
import org.jellyfin.androidtv.util.UUIDUtils
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.androidtv.util.apiclient.Response
import org.jellyfin.androidtv.util.sdk.compat.copyWithDisplayPreferencesId
import org.jellyfin.androidtv.util.sdk.compat.copyWithServerId
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.CollectionType
import timber.log.Timber
import java.util.UUID

class ItemLauncher(
	private val navigationRepository: NavigationRepository,
	private val sessionRepository: SessionRepository,
	private val api: ApiClient,
	private val mediaManager: MediaManager,
	private val playbackLauncher: PlaybackLauncher,
	private val playbackHelper: PlaybackHelper,
) {
	fun launchUserView(baseItem: BaseItemDto?) {
		Timber.d("**** Collection type: %s", baseItem?.collectionType)
		val item = baseItem ?: return
		val destination = getUserViewDestination(item)
		navigationRepository.navigate(destination)
	}

	fun getUserViewDestination(baseItem: BaseItemDto): Destination.Fragment {
		val collectionType = baseItem.collectionType ?: CollectionType.UNKNOWN
		return when (collectionType) {
			CollectionType.MOVIES,
			CollectionType.TVSHOWS,
			-> Destinations.libraryBrowser(baseItem)
			CollectionType.MUSIC -> Destinations.musicBrowser(baseItem)
			CollectionType.LIVETV -> Destinations.liveTvBrowser(baseItem)
			else -> Destinations.libraryBrowser(baseItem)
		}
	}

	fun launch(
		rowItem: BaseRowItem,
		context: Context,
	) {
		var serverId: UUID? = null
		var userId: UUID? = null
		if (rowItem is AggregatedItemBaseRowItem) {
			serverId = rowItem.server.id
			userId = rowItem.userId
		} else {
			val item = rowItem.baseItem
			if (item?.serverId != null) {
				serverId = UUIDUtils.parseUUID(item.serverId)
			}
		}

		when (rowItem.baseRowType) {
			BaseRowType.BaseItem -> launchBaseItem(rowItem, context, serverId, userId)
			BaseRowType.Person -> {
				val itemId = rowItem.itemId ?: return
				navigationRepository.navigate(Destinations.itemDetails(itemId, serverId))
			}
			BaseRowType.Chapter -> {
				val chapter = (rowItem as ChapterItemInfoBaseRowItem).chapterInfo
				val itemId = rowItem.itemId ?: return
				ItemLauncherHelper.getItem(
					itemId,
					serverId,
					object : Response<BaseItemDto>() {
						override fun onResponse(response: BaseItemDto) {
							if (!isActive) return
							val start = chapter.startPositionTicks / 10000
							playbackLauncher.launch(context, listOf(response), start.toInt())
						}
					},
				)
			}
			BaseRowType.LiveTvProgram -> launchLiveTvProgram(rowItem, context, serverId)
			BaseRowType.LiveTvChannel -> {
				val channel = rowItem.baseItem ?: return
				ItemLauncherHelper.getItem(
					channel.id,
					serverId,
					object : Response<BaseItemDto>() {
						override fun onResponse(response: BaseItemDto) {
							if (!isActive) return
							playbackHelper.getItemsToPlay(
								context,
								response,
								false,
								false,
								object : Response<List<BaseItemDto>>() {
									override fun onResponse(response: List<BaseItemDto>) {
										if (!isActive) return
										playbackLauncher.launch(context, response)
									}
								},
							)
						}
					},
				)
			}
			BaseRowType.LiveTvRecording -> launchLiveTvRecording(rowItem, context, serverId)
			BaseRowType.SeriesTimer -> {
				val itemId = rowItem.itemId ?: return
				navigationRepository.navigate(
					Destinations.seriesTimerDetails(
						itemId,
						(rowItem as SeriesTimerInfoDtoBaseRowItem).seriesTimerInfo,
					),
				)
			}
			BaseRowType.GridButton -> {
				when ((rowItem as GridButtonBaseRowItem).gridButton.id) {
					LiveTvOption.LIVE_TV_GUIDE_OPTION_ID -> navigationRepository.navigate(Destinations.liveTvGuide)
					LiveTvOption.LIVE_TV_RECORDINGS_OPTION_ID -> navigationRepository.navigate(Destinations.liveTvRecordings)
					LiveTvOption.LIVE_TV_SERIES_OPTION_ID -> navigationRepository.navigate(Destinations.liveTvSeriesRecordings)
					LiveTvOption.LIVE_TV_SCHEDULE_OPTION_ID -> navigationRepository.navigate(Destinations.liveTvSchedule)
				}
			}
		}
	}

	private fun launchBaseItem(
		rowItem: BaseRowItem,
		context: Context,
		serverId: UUID?,
		userId: UUID?,
	) {
		var baseItem = rowItem.baseItem ?: return
		try {
			Timber.i("Item selected: %s (%s)", baseItem.name, baseItem.type.toString())
		} catch (_: Exception) {
		}

		// Check if this is a Jellyseerr item and handle it specially
		val taglines = baseItem.taglines
		if ("jellyseerr" == baseItem.serverId && !taglines.isNullOrEmpty()) {
			val jellyseerrJson = taglines[0]
			navigationRepository.navigate(Destinations.jellyseerrMediaDetails(jellyseerrJson))
			return
		}

		// Specialized type handling
		when (baseItem.type) {
			BaseItemKind.USER_VIEW,
			BaseItemKind.COLLECTION_FOLDER,
			-> {
				launchUserView(baseItem)
				return
			}
			BaseItemKind.FOLDER -> {
				navigationRepository.navigate(Destinations.folderBrowser(baseItem, serverId))
				return
			}
			BaseItemKind.SERIES,
			BaseItemKind.MUSIC_ARTIST,
			BaseItemKind.MUSIC_ALBUM,
			BaseItemKind.PLAYLIST,
			-> {
				navigationRepository.navigate(Destinations.itemDetails(baseItem.id, serverId))
				return
			}
			BaseItemKind.AUDIO -> {
				val audioItem = rowItem.baseItem ?: return

				if (mediaManager.hasAudioQueueItems() && rowItem is AudioQueueBaseRowItem && audioItem.id == mediaManager.currentAudioItem?.id) {
					navigationRepository.navigate(Destinations.nowPlaying)
				} else if (mediaManager.hasAudioQueueItems() && rowItem is AudioQueueBaseRowItem) {
					mediaManager.playFrom(rowItem.queueEntry)
				} else {
					playbackLauncher.launch(context, listOf(audioItem))
				}
				return
			}
			BaseItemKind.SEASON -> {
				navigationRepository.navigate(Destinations.folderBrowser(baseItem, serverId, userId))
				return
			}
			BaseItemKind.BOX_SET -> {
				navigationRepository.navigate(Destinations.itemDetails(baseItem.id, serverId))
				return
			}
			BaseItemKind.PHOTO -> {
				navigationRepository.navigate(Destinations.photoPlayer(baseItem.id, false, null, null))
				return
			}
			else -> { /* fall through to generic handling */ }
		}

		// Generic handling
		if (Utils.isTrue(baseItem.isFolder)) {
			if (baseItem.displayPreferencesId == null) {
				baseItem = baseItem.copyWithDisplayPreferencesId(baseItem.id.toString())
			}
			navigationRepository.navigate(Destinations.libraryBrowser(baseItem, serverId, userId))
		} else {
			when (rowItem.selectAction) {
				BaseRowItemSelectAction.ShowDetails -> {
					navigationRepository.navigate(Destinations.itemDetails(baseItem.id, serverId))
				}
				BaseRowItemSelectAction.Play -> {
					val finalServerId = serverId
					playbackHelper.getItemsToPlay(
						context,
						baseItem,
						baseItem.type == BaseItemKind.MOVIE,
						false,
						object : Response<List<BaseItemDto>>() {
							override fun onResponse(response: List<BaseItemDto>) {
								if (!isActive) return
								val itemsToPlay: List<BaseItemDto> =
									if (finalServerId != null) {
										response
											.map { item ->
												val serverIdString = finalServerId.toString()
												item.copyWithServerId(serverIdString)
											}
									} else {
										response
									}
								playbackLauncher.launch(context, itemsToPlay)
							}
						},
					)
				}
			}
		}
	}

	private fun launchLiveTvProgram(
		rowItem: BaseRowItem,
		context: Context,
		serverId: UUID?,
	) {
		val program = rowItem.baseItem ?: return
		when (rowItem.selectAction) {
			BaseRowItemSelectAction.ShowDetails -> {
				val channelId = program.channelId ?: return
				navigationRepository.navigate(Destinations.channelDetails(program.id, channelId, program))
			}
			BaseRowItemSelectAction.Play -> {
				val channelId = program.channelId ?: return
				ItemLauncherHelper.getItem(
					channelId,
					serverId,
					object : Response<BaseItemDto>() {
						override fun onResponse(response: BaseItemDto) {
							if (!isActive) return
							playbackLauncher.launch(context, listOf(response))
						}
					},
				)
			}
		}
	}

	private fun launchLiveTvRecording(
		rowItem: BaseRowItem,
		context: Context,
		serverId: UUID?,
	) {
		val item = rowItem.baseItem ?: return
		when (rowItem.selectAction) {
			BaseRowItemSelectAction.ShowDetails -> {
				navigationRepository.navigate(Destinations.itemDetails(item.id, serverId))
			}
			BaseRowItemSelectAction.Play -> {
				ItemLauncherHelper.getItem(
					item.id,
					serverId,
					object : Response<BaseItemDto>() {
						override fun onResponse(response: BaseItemDto) {
							if (!isActive) return
							playbackLauncher.launch(context, listOf(response))
						}
					},
				)
			}
		}
	}
}
