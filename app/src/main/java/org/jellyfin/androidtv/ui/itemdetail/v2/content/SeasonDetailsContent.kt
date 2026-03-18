package org.jellyfin.androidtv.ui.itemdetail.v2.content

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.DetailSectionDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailActionCallbacks
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailContentScaffold
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.DetailEpisodesHorizontalSection
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.detailEpisodesHeader
import org.jellyfin.androidtv.ui.itemdetail.v2.shared.detailSectionRow
import org.jellyfin.sdk.api.client.ApiClient
import java.util.UUID

@Composable
fun SeasonDetailsContent(
	uiState: ItemDetailsUiState,
	contentFocusRequester: FocusRequester,
	api: ApiClient,
	actionCallbacks: DetailActionCallbacks,
	onNavigateToItem: (UUID) -> Unit,
) {
	val item = uiState.item ?: return
	val playButtonFocusRequester = remember { FocusRequester() }

	// Resolve strings in Composable context
	val episodeCountText =
		stringResource(
			if (uiState.episodes.size == 1) R.string.lbl_episode_count_singular else R.string.lbl_episodes_count,
			uiState.episodes.size,
		)

	DetailContentScaffold(
		item = item,
		uiState = uiState,
		api = api,
		actionCallbacks = actionCallbacks,
		genreTag = item.seriesName ?: "",
		metadataContent = {
			Text(
				text = episodeCountText,
				style = JellyfinTheme.typography.bodyMedium.copy(fontSize = 15.sp),
				color = VegafoXColors.TextSecondary,
			)
		},
		synopsisText = null,
		metadataSection = null,
		playButtonFocusRequester = playButtonFocusRequester,
		sections = { scrollState, heroFocus ->
			if (uiState.episodes.isNotEmpty()) {
				detailEpisodesHeader(
					seasonNumber = item.indexNumber,
					episodeIndex = null,
					totalEpisodes = uiState.episodes.size,
					topPadding = 16.dp,
				)
				detailSectionRow(height = DetailSectionDimensions.episodesRowHeight, lazyListState = scrollState, heroFocusRequester = heroFocus) {
					DetailEpisodesHorizontalSection(
						title = "",
						episodes = uiState.episodes,
						currentEpisodeId = UUID(0, 0),
						api = api,
						onNavigateToItem = onNavigateToItem,
						showHeader = false,
					)
				}
			}
		},
	)
}
