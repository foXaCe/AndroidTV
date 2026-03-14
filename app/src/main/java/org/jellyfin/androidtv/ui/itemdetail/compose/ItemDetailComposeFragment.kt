package org.jellyfin.androidtv.ui.itemdetail.compose

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.model.DataRefreshService
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.home.mediabar.TrailerResolver
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsViewModel
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.androidtv.ui.navigation.Destinations
import org.jellyfin.androidtv.ui.navigation.NavigationRepository
import org.jellyfin.androidtv.ui.playback.MediaManager
import org.jellyfin.androidtv.ui.playback.PlaybackLauncher
import org.jellyfin.androidtv.ui.playback.PrePlaybackTrackSelector
import org.jellyfin.androidtv.ui.playback.ThemeMusicPlayer
import org.jellyfin.androidtv.ui.playlist.showAddToPlaylistDialog
import org.jellyfin.androidtv.util.PlaybackHelper
import org.jellyfin.androidtv.util.Utils
import org.jellyfin.androidtv.util.apiclient.Response
import org.jellyfin.androidtv.util.sdk.TrailerUtils.getExternalTrailerIntent
import org.jellyfin.androidtv.util.sdk.TrailerUtils.hasPlayableTrailers
import org.jellyfin.sdk.api.client.exception.ApiClientException
import org.jellyfin.sdk.api.client.extensions.libraryApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.SeriesTimerInfoDto
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

/**
 * Thin Fragment wrapper for the item detail Compose screen.
 *
 * Responsibilities:
 * - Extract itemId/serverId from arguments
 * - Provide ComposeView with JellyfinTheme
 * - Own playback lifecycle (play, resume, shuffle, trailers)
 * - Theme music lifecycle (start/stop)
 * - Delete confirmation dialog
 *
 * All UI is delegated to [ItemDetailScreen].
 */
class ItemDetailComposeFragment : Fragment() {
	private val viewModel: ItemDetailsViewModel by viewModel()
	private val navigationRepository: NavigationRepository by inject()
	private val playbackHelper: PlaybackHelper by inject()
	private val mediaManager: MediaManager by inject()
	private val userPreferences: UserPreferences by inject()
	private val userSettingPreferences: UserSettingPreferences by inject()
	private val trackSelector: PrePlaybackTrackSelector by inject()
	private val playbackLauncher: PlaybackLauncher by inject()
	private val dataRefreshService: DataRefreshService by inject()
	private val themeMusicPlayer: ThemeMusicPlayer by inject()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View =
		ComposeView(requireContext()).apply {
			setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
			setContent {
				JellyfinTheme {
					ItemDetailScreen(
						viewModel = viewModel,
						playbackCallbacks = createPlaybackCallbacks(),
						createActionCallbacks = ::createActionCallbacks,
						userSettingPreferences = userSettingPreferences,
					)
				}
			}
		}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)

		val itemIdStr = arguments?.getString("ItemId")
		val serverIdStr = arguments?.getString("ServerId")
		val itemId = Utils.uuidOrNull(itemIdStr) ?: return
		val serverId = Utils.uuidOrNull(serverIdStr)

		// Check for Live TV arguments
		val channelIdStr = arguments?.getString("ChannelId")
		val programInfoJson = arguments?.getString("ProgramInfo")
		val seriesTimerJson = arguments?.getString("SeriesTimer")

		when {
			// Channel/Program details (Live TV)
			channelIdStr != null && programInfoJson != null -> {
				val channelId = Utils.uuidOrNull(channelIdStr) ?: return
				val programInfo =
					try {
						Json.Default.decodeFromString<BaseItemDto>(programInfoJson)
					} catch (e: Exception) {
						Timber.e(e, "Failed to parse ProgramInfo JSON")
						return
					}
				viewModel.loadChannelProgram(channelId, programInfo)
			}

			// Series timer details (Live TV)
			seriesTimerJson != null -> {
				val seriesTimer =
					try {
						Json.Default.decodeFromString<SeriesTimerInfoDto>(seriesTimerJson)
					} catch (e: Exception) {
						Timber.e(e, "Failed to parse SeriesTimer JSON")
						return
					}
				viewModel.loadSeriesTimer(seriesTimer)
			}

			// Standard item details
			else -> viewModel.loadItem(itemId, serverId)
		}

		// Theme music lifecycle
		viewModel.uiState
			.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
			.onEach { uiState ->
				val item = uiState.item
				if (item != null) {
					themeMusicPlayer.playThemeMusicForItem(item)
				}
			}.launchIn(lifecycleScope)
	}

	override fun onDestroyView() {
		themeMusicPlayer.stop()
		super.onDestroyView()
	}

	// ---- Callback factories ----

	private fun createPlaybackCallbacks() =
		DetailPlaybackCallbacks(
			onPlay = { item, positionMs, shuffle -> play(item, positionMs, shuffle) },
			onPlayFromHere = { trackIds ->
				playbackHelper.retrieveAndPlay(trackIds, false, null, null, requireContext())
			},
			onPlaySingle = { trackId ->
				playbackHelper.retrieveAndPlay(trackId, false, requireContext())
			},
			onPlayInstantMix = { item ->
				playbackHelper.playInstantMix(requireContext(), item)
			},
			onQueueAudioItem = { item ->
				mediaManager.queueAudioItem(item)
			},
			onPlayTrailers = { item -> playTrailers(item) },
			onConfirmDelete = { item -> confirmDeleteItem(item) },
			onAddToPlaylist = { item -> showAddToPlaylistDialog(requireContext(), item.id) },
			onNavigateToItem = { id ->
				navigationRepository.navigate(Destinations.itemDetails(id, viewModel.serverId))
			},
		)

	private fun createActionCallbacks(
		item: BaseItemDto,
		uiState: ItemDetailsUiState,
		context: android.content.Context,
	): DetailActionCallbacks =
		DetailActionCallbacks(
			trackSelector = trackSelector,
			hasPlayableTrailers = hasPlayableTrailers(context, item),
			onPlay = { handlePlay(item, uiState) },
			onResume = { handleResume(item) },
			onShuffle = { handleShuffle(item) },
			onPlayTrailers = { playTrailers(item) },
			onPlayInstantMix = { playbackHelper.playInstantMix(context, item) },
			onToggleWatched = { viewModel.toggleWatched() },
			onToggleFavorite = { viewModel.toggleFavorite() },
			onConfirmDelete = { confirmDeleteItem(item) },
			onAddToPlaylist = { showAddToPlaylistDialog(context, item.id) },
			onGoToSeries =
				if (item.type == BaseItemKind.EPISODE && item.seriesId != null) {
					{
						item.seriesId?.let { seriesId ->
							navigationRepository.navigate(Destinations.itemDetails(seriesId, viewModel.serverId))
						}
					}
				} else {
					null
				},
			onLoadItem = { id -> viewModel.loadItem(id) },
		)

	// ---- Playback helpers ----

	private fun play(
		item: BaseItemDto,
		positionMs: Int,
		shuffle: Boolean,
	) {
		playbackHelper.getItemsToPlay(
			requireContext(),
			item,
			positionMs == 0 && item.type == BaseItemKind.MOVIE,
			shuffle,
			object : Response<List<BaseItemDto>>(lifecycle) {
				override fun onResponse(response: List<BaseItemDto>) {
					if (!isActive) return
					if (response.isEmpty()) {
						Timber.e("No items to play - ignoring play request.")
						return
					}
					playbackLauncher.launch(requireContext(), response, positionMs, false, 0, shuffle)
				}
			},
		)
	}

	private fun handlePlay(
		item: BaseItemDto,
		uiState: ItemDetailsUiState,
	) {
		when (item.type) {
			BaseItemKind.SERIES -> {
				if (uiState.nextUp.isNotEmpty()) {
					play(uiState.nextUp.first(), 0, false)
				} else {
					play(item, 0, false)
				}
			}
			BaseItemKind.SEASON -> {
				if (uiState.episodes.isNotEmpty()) {
					val unwatched = uiState.episodes.firstOrNull { !(it.userData?.played ?: false) }
					val episode = unwatched ?: uiState.episodes.first()
					play(episode, 0, false)
				}
			}
			else -> play(item, 0, false)
		}
	}

	private fun handleResume(item: BaseItemDto) {
		val prerollMs = (userPreferences[UserPreferences.resumeSubtractDuration].toIntOrNull() ?: 0) * 1000
		val posMs = ((item.userData?.playbackPositionTicks ?: 0L) / 10_000).toInt()
		val position = maxOf(posMs - prerollMs, 0)
		play(item, position, false)
	}

	private fun handleShuffle(item: BaseItemDto) {
		play(item, 0, true)
	}

	private fun playTrailers(item: BaseItemDto) {
		val localTrailerCount = item.localTrailerCount ?: 0

		if (localTrailerCount < 1) {
			lifecycleScope.launch {
				try {
					val trailerInfo =
						withContext(Dispatchers.IO) {
							TrailerResolver.resolveTrailerFromItem(item)
						}

					if (trailerInfo != null) {
						val segmentsJson =
							trailerInfo.segments.joinToString(",", "[", "]") { seg ->
								"""{"start":${seg.startTime},"end":${seg.endTime},"category":"${seg.category}","action":"${seg.actionType}"}"""
							}
						navigationRepository.navigate(
							Destinations.trailerPlayer(
								videoId = trailerInfo.youtubeVideoId,
								startSeconds = trailerInfo.startSeconds,
								segmentsJson = segmentsJson,
							),
						)
					} else {
						val intent = getExternalTrailerIntent(requireContext(), item)
						if (intent != null) {
							startActivity(Intent.createChooser(intent, getString(R.string.lbl_play_trailers)))
						} else {
							Toast.makeText(requireContext(), getString(R.string.no_player_message), Toast.LENGTH_LONG).show()
						}
					}
				} catch (e: Exception) {
					Timber.w(e, "Failed to resolve trailer")
					try {
						val intent = getExternalTrailerIntent(requireContext(), item)
						if (intent != null) {
							startActivity(Intent.createChooser(intent, getString(R.string.lbl_play_trailers)))
						}
					} catch (e2: ActivityNotFoundException) {
						Timber.w(e2, "Unable to open external trailer")
						Toast.makeText(requireContext(), getString(R.string.no_player_message), Toast.LENGTH_LONG).show()
					}
				}
			}
		} else {
			lifecycleScope.launch {
				try {
					val trailers =
						withContext(Dispatchers.IO) {
							viewModel.effectiveApi.userLibraryApi
								.getLocalTrailers(itemId = item.id)
								.content
						}
					if (trailers.isNotEmpty()) {
						playbackHelper.retrieveAndPlay(trailers.map { it.id }, false, null, null, requireContext())
					}
				} catch (e: ApiClientException) {
					Timber.e(e, "Error retrieving trailers for playback")
					Toast.makeText(requireContext(), getString(R.string.msg_video_playback_error), Toast.LENGTH_LONG).show()
				}
			}
		}
	}

	private fun confirmDeleteItem(item: BaseItemDto) {
		android.app.AlertDialog
			.Builder(requireContext())
			.setTitle(R.string.item_delete_confirm_title)
			.setMessage(R.string.item_delete_confirm_message)
			.setNegativeButton(R.string.lbl_no, null)
			.setPositiveButton(R.string.lbl_delete) { _, _ -> deleteItem(item) }
			.show()
	}

	private fun deleteItem(item: BaseItemDto) {
		lifecycleScope.launch {
			try {
				withContext(Dispatchers.IO) {
					viewModel.effectiveApi.libraryApi.deleteItem(itemId = item.id)
				}
			} catch (e: ApiClientException) {
				Timber.e(e, "Failed to delete item ${item.name} (id=${item.id})")
				Toast.makeText(requireContext(), getString(R.string.item_deletion_failed, item.name), Toast.LENGTH_LONG).show()
				return@launch
			}
			dataRefreshService.lastDeletedItemId = item.id
			if (navigationRepository.canGoBack) {
				navigationRepository.goBack()
			} else {
				navigationRepository.navigate(Destinations.home)
			}
			Toast.makeText(requireContext(), getString(R.string.item_deleted, item.name), Toast.LENGTH_LONG).show()
		}
	}
}
