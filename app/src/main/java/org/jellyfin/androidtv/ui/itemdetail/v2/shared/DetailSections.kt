package org.jellyfin.androidtv.ui.itemdetail.v2.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.DetailSectionDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.itemdetail.v2.CastCard
import org.jellyfin.androidtv.ui.itemdetail.v2.EpisodeCard
import org.jellyfin.androidtv.ui.itemdetail.v2.ItemDetailsUiState
import org.jellyfin.androidtv.ui.itemdetail.v2.LandscapeItemCard
import org.jellyfin.androidtv.ui.itemdetail.v2.MetadataGroup
import org.jellyfin.androidtv.ui.itemdetail.v2.SeasonCard
import org.jellyfin.androidtv.ui.itemdetail.v2.SectionHeader
import org.jellyfin.androidtv.ui.itemdetail.v2.SimilarItemCard
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.imageApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemPerson
import org.jellyfin.sdk.model.api.ImageType
import java.util.UUID

@Composable
fun DetailMetadataSection(
	item: BaseItemDto,
	uiState: ItemDetailsUiState,
) {
	val metaItems = mutableListOf<Pair<String, String>>()

	if (uiState.directors.isNotEmpty()) {
		metaItems.add(stringResource(R.string.lbl_director) to uiState.directors.joinToString(", ") { it.name ?: "" })
	}
	if (uiState.writers.isNotEmpty()) {
		metaItems.add(stringResource(R.string.lbl_writers) to uiState.writers.joinToString(", ") { it.name ?: "" })
	}
	val studios = item.studios ?: emptyList()
	if (studios.isNotEmpty()) {
		metaItems.add(stringResource(R.string.lbl_studio) to studios.joinToString(", ") { it.name ?: "" })
	}

	if (metaItems.isNotEmpty()) {
		MetadataGroup(items = metaItems)
		Spacer(modifier = Modifier.height(24.dp))
	}
}

@Composable
fun DetailSeasonsSection(
	seasons: List<BaseItemDto>,
	api: ApiClient,
	onNavigateToItem: (UUID) -> Unit,
	showHeader: Boolean = true,
) {
	Column {
		if (showHeader) SectionHeader(title = stringResource(R.string.lbl_seasons))
		LazyRow(
			modifier = Modifier.focusRestorer().focusGroup(),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			contentPadding = PaddingValues(horizontal = 0.dp),
		) {
			items(seasons, key = { it.id }) { season ->
				val imageUrl = remember(season.id) { getPosterUrl(season, api) }
				SeasonCard(
					name = season.name ?: stringResource(R.string.lbl_seasons),
					imageUrl = imageUrl,
					isWatched = season.userData?.played == true,
					unplayedCount = season.userData?.unplayedItemCount,
					onClick = { onNavigateToItem(season.id) },
					item = season,
				)
			}
		}
	}
}

@Composable
fun DetailEpisodesHeader(
	seasonNumber: Int?,
	episodeIndex: Int?,
	totalEpisodes: Int,
	modifier: Modifier = Modifier,
) {
	val episodesLabel = stringResource(R.string.lbl_episodes).uppercase()

	Row(
		modifier = modifier.padding(bottom = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		if (seasonNumber != null) {
			Text(
				text = "SAISON $seasonNumber",
				fontSize = 22.sp,
				fontFamily = BebasNeue,
				color = VegafoXColors.OrangePrimary,
			)
			Text(
				text = "\u00B7",
				fontSize = 22.sp,
				fontFamily = BebasNeue,
				color = VegafoXColors.TextSecondary,
			)
		}
		Text(
			text = "\u00C9pisodes ${ episodeIndex?.let { "$it/" } ?: "" }$totalEpisodes",
			fontSize = 22.sp,
			fontFamily = BebasNeue,
			color = VegafoXColors.TextPrimary,
		)
	}
}

@Composable
fun DetailEpisodesHorizontalSection(
	title: String,
	episodes: List<BaseItemDto>,
	currentEpisodeId: UUID,
	api: ApiClient,
	onNavigateToItem: (UUID) -> Unit,
	showHeader: Boolean = true,
) {
	Column {
		if (showHeader) SectionHeader(title = title)
		LazyRow(
			modifier = Modifier.focusRestorer().focusGroup(),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			contentPadding = PaddingValues(horizontal = 0.dp),
		) {
			items(episodes, key = { it.id }) { ep ->
				val imageUrl = remember(ep.id) { getEpisodeThumbnailUrl(ep, api) ?: getPosterUrl(ep, api) }
				EpisodeCard(
					episodeNumber = ep.indexNumber,
					title = ep.name ?: "",
					imageUrl = imageUrl,
					progress = ep.userData?.playedPercentage ?: 0.0,
					isCurrent = ep.id == currentEpisodeId,
					isPlayed = ep.userData?.played == true,
					onClick = { onNavigateToItem(ep.id) },
				)
			}
		}
	}
}

@Composable
fun DetailCastSection(
	cast: List<BaseItemPerson>,
	api: ApiClient,
	onNavigateToItem: (UUID) -> Unit,
	showHeader: Boolean = true,
) {
	Column {
		if (showHeader) SectionHeader(title = stringResource(R.string.lbl_cast_crew))
		LazyRow(
			modifier = Modifier.focusRestorer().focusGroup(),
			horizontalArrangement = Arrangement.spacedBy(DetailSectionDimensions.castCardGap),
			contentPadding = PaddingValues(horizontal = 0.dp),
		) {
			items(cast, key = { it.id }) { person ->
				val imageUrl =
					remember(person.id, person.primaryImageTag) {
						person.primaryImageTag?.let { tag ->
							api.imageApi.getItemImageUrl(
								itemId = person.id,
								imageType = ImageType.PRIMARY,
								tag = tag,
								maxHeight = 280,
							)
						}
					}
				CastCard(
					name = person.name ?: "",
					role = person.role ?: person.type.toString(),
					imageUrl = imageUrl,
					onClick = { onNavigateToItem(person.id) },
				)
			}
		}
	}
}

@Composable
fun DetailSectionWithCards(
	title: String,
	items: List<BaseItemDto>,
	api: ApiClient,
	onNavigateToItem: (UUID) -> Unit,
	isLandscape: Boolean = false,
	firstItemFocusRequester: FocusRequester? = null,
	onItemFocused: ((BaseItemDto) -> Unit)? = null,
	showHeader: Boolean = true,
) {
	Column {
		if (showHeader) SectionHeader(title = title)
		LazyRow(
			modifier = Modifier.focusRestorer().focusGroup(),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			contentPadding = PaddingValues(horizontal = 0.dp),
		) {
			items(items.size) { index ->
				val item = items[index]
				val cardModifier =
					if (index == 0 && firstItemFocusRequester != null) {
						Modifier.focusRequester(firstItemFocusRequester)
					} else {
						Modifier
					}

				if (isLandscape) {
					val imageUrl = remember(item.id) { getEpisodeThumbnailUrl(item, api) }
					LandscapeItemCard(
						title = item.name ?: "",
						imageUrl = imageUrl,
						subtitle = item.seriesName,
						onClick = { onNavigateToItem(item.id) },
						onFocused = onItemFocused?.let { callback -> { callback(item) } },
						modifier = cardModifier,
						item = item,
					)
				} else {
					val imageUrl = remember(item.id) { getPosterUrl(item, api) }
					SimilarItemCard(
						title = item.name ?: "",
						imageUrl = imageUrl,
						year = item.productionYear,
						onClick = { onNavigateToItem(item.id) },
						onFocused = onItemFocused?.let { callback -> { callback(item) } },
						modifier = cardModifier,
						item = item,
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailCollectionItemsGrid(
	items: List<BaseItemDto>,
	api: ApiClient,
	onNavigateToItem: (UUID) -> Unit,
	firstItemFocusRequester: FocusRequester? = null,
) {
	Column {
		SectionHeader(title = stringResource(R.string.lbl_items_in_collection))
		FlowRow(
			modifier = Modifier.focusGroup(),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			items.forEachIndexed { index, item ->
				val cardModifier =
					if (index == 0 && firstItemFocusRequester != null) {
						Modifier.focusRequester(firstItemFocusRequester)
					} else {
						Modifier
					}

				SimilarItemCard(
					title = item.name ?: "",
					imageUrl = getPosterUrl(item, api),
					year = item.productionYear,
					onClick = { onNavigateToItem(item.id) },
					modifier = cardModifier,
					item = item,
				)
			}
		}
	}
}

@Composable
fun DetailPlaylistHint() {
	Row(
		modifier =
			Modifier
				.fillMaxWidth()
				.clip(JellyfinTheme.shapes.small)
				.background(JellyfinTheme.colorScheme.outlineVariant)
				.border(
					1.dp,
					JellyfinTheme.colorScheme.outlineVariant,
					JellyfinTheme.shapes.small,
				).padding(vertical = 12.dp, horizontal = 18.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = stringResource(R.string.lbl_reorder_hint),
			style = JellyfinTheme.typography.bodyMedium,
			color = JellyfinTheme.colorScheme.onSurfaceVariant,
		)
	}
}
