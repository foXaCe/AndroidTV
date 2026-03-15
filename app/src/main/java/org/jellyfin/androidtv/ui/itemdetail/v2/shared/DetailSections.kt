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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
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
	val context = androidx.compose.ui.platform.LocalContext.current
	val metaItems = mutableListOf<Pair<String, String>>()

	val genres = item.genres ?: emptyList()
	if (genres.isNotEmpty()) {
		metaItems.add(stringResource(R.string.lbl_genres) to genres.take(3).joinToString(", ") { translateGenre(context, it) })
	}
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
) {
	Column {
		SectionHeader(title = stringResource(R.string.lbl_seasons))
		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			contentPadding = PaddingValues(horizontal = 0.dp),
		) {
			items(seasons, key = { it.id }) { season ->
				SeasonCard(
					name = season.name ?: stringResource(R.string.lbl_seasons),
					imageUrl = getPosterUrl(season, api),
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
fun DetailEpisodesHorizontalSection(
	title: String,
	episodes: List<BaseItemDto>,
	currentEpisodeId: UUID,
	api: ApiClient,
	onNavigateToItem: (UUID) -> Unit,
) {
	Column {
		SectionHeader(title = title)
		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			contentPadding = PaddingValues(horizontal = 0.dp),
		) {
			items(episodes, key = { it.id }) { ep ->
				EpisodeCard(
					episodeNumber = ep.indexNumber,
					title = ep.name ?: "",
					runtime = ep.runTimeTicks?.let { formatDuration(it) },
					imageUrl = getPosterUrl(ep, api),
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
) {
	Column {
		SectionHeader(title = stringResource(R.string.lbl_cast_crew))
		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(24.dp),
			contentPadding = PaddingValues(horizontal = 0.dp),
		) {
			items(cast, key = { it.id }) { person ->
				CastCard(
					name = person.name ?: "",
					role = person.role ?: person.type.toString(),
					imageUrl =
						person.primaryImageTag?.let { tag ->
							api.imageApi.getItemImageUrl(
								itemId = person.id,
								imageType = ImageType.PRIMARY,
								tag = tag,
								maxHeight = 280,
							)
						},
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
) {
	Column {
		SectionHeader(title = title)
		LazyRow(
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
					LandscapeItemCard(
						title = item.name ?: "",
						imageUrl = getEpisodeThumbnailUrl(item, api),
						subtitle = item.seriesName,
						onClick = { onNavigateToItem(item.id) },
						onFocused = onItemFocused?.let { callback -> { callback(item) } },
						modifier = cardModifier,
						item = item,
					)
				} else {
					SimilarItemCard(
						title = item.name ?: "",
						imageUrl = getPosterUrl(item, api),
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
