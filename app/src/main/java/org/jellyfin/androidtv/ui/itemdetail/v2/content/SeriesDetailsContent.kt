package org.jellyfin.androidtv.ui.itemdetail.v2.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.theme.DetailSectionDimensions
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailCastSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailContentScaffold
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailMetadataSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSeasonsSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailSectionWithCards
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.detailSectionHeader
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.detailSectionRow
import org.jellyfin.androidtv.ui.shared.components.MediaMetadataBadges
import org.jellyfin.sdk.api.client.ApiClient
import java.util.UUID

/**
 * Cinema Immersive detail content for Series type.
 */
@Composable
fun SeriesDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	api: ApiClient,
	actionCallbacks: DetailActionCallbacks,
	onNavigateToItem: (UUID) -> Unit,
) {
	val item = uiState.item ?: return
	val playButtonFocusRequester = remember { FocusRequester() }

	val genreTag = buildSeriesGenreTag(item)

	// Resolve strings in Composable context
	val nextUpTitle = stringResource(R.string.lbl_next_up)
	val seasonsTitle = stringResource(R.string.lbl_seasons)
	val castTitle = stringResource(R.string.lbl_cast_crew)
	val similarTitle = stringResource(R.string.lbl_more_like_this)

	DetailContentScaffold(
		item = item,
		uiState = uiState,
		api = api,
		actionCallbacks = actionCallbacks,
		genreTag = genreTag,
		metadataContent = { MediaMetadataBadges(item = item) },
		synopsisText = item.overview,
		metadataSection = { DetailMetadataSection(item = item, uiState = uiState) },
		playButtonFocusRequester = playButtonFocusRequester,
		sections = { scrollState, heroFocus ->
			// Next Up
			if (uiState.nextUp.isNotEmpty()) {
				detailSectionHeader(title = nextUpTitle, topPadding = 16.dp)
				detailSectionRow(height = DetailSectionDimensions.episodesRowHeight, lazyListState = scrollState, heroFocusRequester = heroFocus) {
					DetailSectionWithCards(
						title = "",
						items = uiState.nextUp,
						api = api,
						onNavigateToItem = onNavigateToItem,
						isLandscape = true,
						showHeader = false,
					)
				}
			}

			// Seasons
			if (uiState.seasons.isNotEmpty()) {
				detailSectionHeader(title = seasonsTitle)
				detailSectionRow(height = DetailSectionDimensions.seasonsRowHeight, lazyListState = scrollState, heroFocusRequester = heroFocus) {
					DetailSeasonsSection(uiState.seasons, api, onNavigateToItem, showHeader = false)
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

private fun buildSeriesGenreTag(item: org.jellyfin.sdk.model.api.BaseItemDto): String {
	val genre = item.genres?.firstOrNull()
	return listOfNotNull("S\u00C9RIE", genre?.uppercase()).joinToString("  \u2022  ")
}
