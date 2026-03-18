package org.jellyfin.androidtv.ui.itemdetail.compose

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.jellyfin.androidtv.preference.UserSettingPreferences
import org.jellyfin.androidtv.ui.base.CircularProgressIndicator
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsViewModel
import org.jellyfin.androidtv.ui.itemdetail.v2.content.LiveTvDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.MovieDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.MusicDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.PersonDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.SeasonDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.SeriesDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.content.SeriesTimerDetailsContent
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

/**
 * Callbacks for playback actions that require Fragment context.
 * These are provided by [ItemDetailComposeFragment].
 */
data class DetailPlaybackCallbacks(
	val onPlay: (BaseItemDto, Int, Boolean) -> Unit,
	val onPlayFromHere: (List<UUID>) -> Unit,
	val onPlaySingle: (UUID) -> Unit,
	val onPlayInstantMix: (BaseItemDto) -> Unit,
	val onQueueAudioItem: (BaseItemDto) -> Unit,
	val onPlayTrailers: (BaseItemDto) -> Unit,
	val onConfirmDelete: (BaseItemDto) -> Unit,
	val onNavigateToItem: (UUID) -> Unit,
)

/**
 * Main item detail screen — Compose-first with integrated backdrop.
 *
 * Architecture:
 * ```
 * Box (fullscreen) {
 *     DetailHeroBackdrop(item)     // fullscreen image + gradients
 *     when (item.type) {           // type-specific content
 *         MOVIE    -> MovieDetailsContent
 *         SERIES   -> SeriesDetailsContent
 *         MUSIC_*  -> MusicDetailsContent
 *         PERSON   -> PersonDetailsContent
 *         SEASON   -> SeasonDetailsContent
 *     }
 * }
 * ```
 *
 * The backdrop is shown for Movie/Series/Episode types.
 * Person/Music/Playlist handle their own slideshow backdrops.
 */
@Composable
fun ItemDetailScreen(
	viewModel: ItemDetailsViewModel,
	playbackCallbacks: DetailPlaybackCallbacks,
	createActionCallbacks: (BaseItemDto, ItemDetailsUiState, Context) -> DetailActionCallbacks,
	userSettingPreferences: UserSettingPreferences,
) {
	val uiState by viewModel.uiState.collectAsState()
	val contentFocusRequester = remember { FocusRequester() }

	if (uiState.isLoading) {
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center,
		) {
			CircularProgressIndicator()
		}
		return
	}

	if (uiState.error != null && uiState.item == null) {
		ErrorState(
			message = stringResource(uiState.error!!.messageRes),
			onRetry = { viewModel.retry() },
		)
		return
	}

	val item = uiState.item ?: return
	val api = viewModel.effectiveApi
	val context = LocalContext.current
	val blurAmount = userSettingPreferences[UserSettingPreferences.detailsBackgroundBlurAmount]

	// Show the Compose backdrop for types that don't have their own slideshow
	val showHeroBackdrop =
		item.type != BaseItemKind.PERSON &&
			item.type != BaseItemKind.PLAYLIST

	Box(modifier = Modifier.fillMaxSize()) {
		// Layer 1: Hero backdrop
		if (showHeroBackdrop) {
			DetailHeroBackdrop(
				item = item,
				api = api,
				blurAmount = blurAmount,
			)
		}

		// Layer 2: Content dispatch by type
		when (item.type) {
			BaseItemKind.PERSON ->
				PersonDetailsContent(
					uiState = uiState,
					contentFocusRequester = contentFocusRequester,
					showBackdrop = true,
					api = api,
					onNavigateToItem = playbackCallbacks.onNavigateToItem,
				)

			BaseItemKind.SEASON -> {
				val actionCallbacks = createActionCallbacks(item, uiState, context)
				SeasonDetailsContent(
					uiState = uiState,
					contentFocusRequester = contentFocusRequester,
					api = api,
					actionCallbacks = actionCallbacks,
					onNavigateToItem = playbackCallbacks.onNavigateToItem,
				)
			}

			BaseItemKind.SERIES -> {
				val actionCallbacks = createActionCallbacks(item, uiState, context)
				SeriesDetailsContent(
					uiState = uiState,
					contentFocusRequester = contentFocusRequester,
					api = api,
					actionCallbacks = actionCallbacks,
					onNavigateToItem = playbackCallbacks.onNavigateToItem,
				)
			}

			BaseItemKind.MUSIC_ALBUM, BaseItemKind.MUSIC_ARTIST, BaseItemKind.PLAYLIST -> {
				val actionCallbacks = createActionCallbacks(item, uiState, context)
				MusicDetailsContent(
					uiState = uiState,
					contentFocusRequester = contentFocusRequester,
					api = api,
					actionCallbacks = actionCallbacks,
					onNavigateToItem = playbackCallbacks.onNavigateToItem,
					onPlayFromHere = playbackCallbacks.onPlayFromHere,
					onPlaySingle = playbackCallbacks.onPlaySingle,
					onPlayInstantMix = playbackCallbacks.onPlayInstantMix,
					onQueueAudioItem = playbackCallbacks.onQueueAudioItem,
					onMovePlaylistItem = { from, to ->
						viewModel.movePlaylistItem(from, to)
					},
					onRemoveFromPlaylist = { index ->
						viewModel.removeFromPlaylist(index)
					},
				)
			}

			else ->
				when {
					// Series timer details (detected by presence of seriesTimerInfo)
					uiState.seriesTimerInfo != null ->
						SeriesTimerDetailsContent(
							uiState = uiState,
							contentFocusRequester = contentFocusRequester,
							api = api,
							onCancelSeriesTimer = { viewModel.cancelSeriesTimer() },
							onNavigateToItem = playbackCallbacks.onNavigateToItem,
						)

					// Live TV program details
					item.type == BaseItemKind.PROGRAM ->
						LiveTvDetailsContent(
							uiState = uiState,
							contentFocusRequester = contentFocusRequester,
							api = api,
							onPlay = { playbackCallbacks.onPlay(item, 0, false) },
							onToggleRecord = { viewModel.toggleRecord() },
							onToggleRecordSeries = { viewModel.toggleRecordSeries() },
							onNavigateToItem = playbackCallbacks.onNavigateToItem,
						)

					else -> {
						val actionCallbacks = createActionCallbacks(item, uiState, context)
						MovieDetailsContent(
							uiState = uiState,
							contentFocusRequester = contentFocusRequester,
							api = api,
							actionCallbacks = actionCallbacks,
							onNavigateToItem = playbackCallbacks.onNavigateToItem,
						)
					}
				}
		}
	}
}
