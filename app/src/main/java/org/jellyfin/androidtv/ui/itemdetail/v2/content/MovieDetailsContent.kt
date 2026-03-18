package org.jellyfin.androidtv.ui.itemdetail.v2.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.theme.DetailDimensions
import org.jellyfin.androidtv.ui.base.theme.DetailSectionDimensions
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailCastSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailCollectionItemsGrid
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailContentScaffold
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailEpisodesHorizontalSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailMetadataSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSectionWithCards
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.detailEpisodesHeader
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.detailSectionHeader
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.detailSectionRow
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.translateGenre
import org.jellyfin.androidtv.ui.shared.components.MediaMetadataBadges
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemKind
import java.util.UUID

/**
 * Cinema Immersive detail content for Movie, Episode, Video, Recording,
 * Trailer, MusicVideo, and BoxSet types.
 */
@Composable
fun MovieDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	api: ApiClient,
	actionCallbacks: DetailActionCallbacks,
	onNavigateToItem: (UUID) -> Unit,
) {
	val item = uiState.item ?: return
	val context = LocalContext.current
	val playButtonFocusRequester = remember { FocusRequester() }

	val isEpisode = item.type == BaseItemKind.EPISODE
	val isBoxSet = item.type == BaseItemKind.BOX_SET

	val genreTag = buildGenreTag(context, item)

	// Resolve strings in Composable context (before LazyListScope)
	val castTitle = stringResource(R.string.lbl_cast_crew)
	val similarTitle = stringResource(R.string.lbl_more_like_this)

	DetailContentScaffold(
		item = item,
		uiState = uiState,
		api = api,
		actionCallbacks = actionCallbacks,
		genreTag = if (isEpisode) buildEpisodeTag(item) else genreTag,
		metadataContent = { MediaMetadataBadges(item = item) },
		synopsisText = item.overview,
		metadataSection = { DetailMetadataSection(item = item, uiState = uiState) },
		playButtonFocusRequester = playButtonFocusRequester,
		sections = { scrollState, heroFocus ->
			// Episodes section (for EPISODE type — same season)
			if (isEpisode && uiState.episodes.isNotEmpty()) {
				detailEpisodesHeader(
					seasonNumber = item.parentIndexNumber,
					episodeIndex = item.indexNumber,
					totalEpisodes = uiState.episodes.size,
					topPadding = 16.dp,
				)
				detailSectionRow(height = DetailSectionDimensions.episodesRowHeight, lazyListState = scrollState, heroFocusRequester = heroFocus) {
					DetailEpisodesHorizontalSection(
						title = "",
						episodes = uiState.episodes,
						currentEpisodeId = item.id,
						api = api,
						onNavigateToItem = onNavigateToItem,
						showHeader = false,
					)
				}
			}

			// BoxSet collection items
			if (isBoxSet && uiState.collectionItems.isNotEmpty()) {
				item {
					Box(
						modifier =
							Modifier
								.fillMaxWidth()
								.background(Color.Transparent)
								.padding(horizontal = DetailDimensions.contentPaddingHorizontal)
								.padding(top = 16.dp),
					) {
						DetailCollectionItemsGrid(
							items = uiState.collectionItems,
							api = api,
							onNavigateToItem = onNavigateToItem,
						)
					}
				}
			}

			// Cast & Crew
			if (uiState.cast.isNotEmpty()) {
				detailSectionHeader(title = castTitle)
				detailSectionRow(height = DetailSectionDimensions.castRowHeight, lazyListState = scrollState, heroFocusRequester = heroFocus) {
					DetailCastSection(uiState.cast, api, onNavigateToItem, showHeader = false)
				}
			}

			// More Like This
			if (uiState.similar.isNotEmpty()) {
				detailSectionHeader(title = similarTitle)
				detailSectionRow(height = DetailSectionDimensions.similarRowHeight, lazyListState = scrollState, heroFocusRequester = heroFocus) {
					DetailSectionWithCards(
						title = "",
						items = uiState.similar,
						api = api,
						onNavigateToItem = onNavigateToItem,
						showHeader = false,
					)
				}
			}
		},
	)
}

// ─── Helpers ───

private fun buildGenreTag(
	context: android.content.Context,
	item: org.jellyfin.sdk.model.api.BaseItemDto,
): String {
	val type =
		when (item.type) {
			BaseItemKind.MOVIE -> "FILM"
			BaseItemKind.EPISODE -> "S\u00C9RIE"
			BaseItemKind.SERIES -> "S\u00C9RIE"
			BaseItemKind.RECORDING -> "ENREGISTREMENT"
			BaseItemKind.TRAILER -> "BANDE-ANNONCE"
			BaseItemKind.MUSIC_VIDEO -> "CLIP"
			BaseItemKind.BOX_SET -> "COLLECTION"
			else -> null
		}
	val genres = item.genres?.take(2)?.map { translateGenre(context, it).uppercase() }
	val parts = listOfNotNull(type) + (genres ?: emptyList())
	return parts.joinToString("  \u2022  ")
}

private fun buildEpisodeTag(item: org.jellyfin.sdk.model.api.BaseItemDto): String {
	val seriesName = item.seriesName ?: ""
	return if (item.parentIndexNumber != null && item.indexNumber != null) {
		"$seriesName  \u2022  S${item.parentIndexNumber}E${item.indexNumber}"
	} else {
		seriesName
	}
}
