package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.JellyseerrRowType
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrGenreDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrMediaInfoDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrNetworkDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrStudioDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.jellyseerr.JellyseerrDetailsViewModel
import org.jellyfin.androidtv.ui.jellyseerr.JellyseerrDiscoverViewModel

/**
 * Compose replacement for the Leanback RowsSupportFragment in JellyseerrDiscoverRowsFragment.
 * Renders a vertical list of horizontal rows, each with typed cards (poster, genre, logo).
 */
@Composable
fun JellyseerrDiscoverRows(
	discoverViewModel: JellyseerrDiscoverViewModel,
	detailsViewModel: JellyseerrDetailsViewModel,
	activeRows: List<JellyseerrRowType>,
	onItemClick: (JellyseerrDiscoverItemDto) -> Unit,
	onItemFocused: (JellyseerrDiscoverItemDto?) -> Unit,
	onGenreClick: (JellyseerrGenreDto, String) -> Unit,
	onStudioClick: (JellyseerrStudioDto) -> Unit,
	onNetworkClick: (JellyseerrNetworkDto) -> Unit,
	modifier: Modifier = Modifier,
) {
	// Collect all StateFlows
	val trending by discoverViewModel.trending.collectAsState()
	val trendingMovies by discoverViewModel.trendingMovies.collectAsState()
	val trendingTv by discoverViewModel.trendingTv.collectAsState()
	val upcomingMovies by discoverViewModel.upcomingMovies.collectAsState()
	val upcomingTv by discoverViewModel.upcomingTv.collectAsState()
	val movieGenres by discoverViewModel.movieGenres.collectAsState()
	val tvGenres by discoverViewModel.tvGenres.collectAsState()
	val studios by discoverViewModel.studios.collectAsState()
	val networks by discoverViewModel.networks.collectAsState()
	val userRequests by detailsViewModel.userRequests.collectAsState()

	// Map requests to discover items
	val unknownLabel = stringResource(R.string.lbl_unknown)
	val requestItems by remember(userRequests) {
		derivedStateOf {
			userRequests.map { request ->
				JellyseerrDiscoverItemDto(
					id = request.media?.tmdbId ?: request.media?.id ?: request.id,
					title = request.media?.title ?: request.media?.name ?: unknownLabel,
					name = request.media?.name ?: request.media?.title ?: unknownLabel,
					overview = request.media?.overview ?: "",
					releaseDate = request.media?.releaseDate,
					firstAirDate = request.media?.firstAirDate,
					mediaType = request.type,
					posterPath = request.media?.posterPath,
					backdropPath = request.media?.backdropPath,
					mediaInfo = JellyseerrMediaInfoDto(
						id = request.media?.id,
						tmdbId = request.media?.tmdbId,
						tvdbId = request.media?.tvdbId,
						status = request.media?.status,
						status4k = request.media?.status4k,
					),
				)
			}.filter { it.posterPath != null || it.backdropPath != null }
		}
	}

	val columnState = rememberLazyListState()

	LazyColumn(
		state = columnState,
		contentPadding = PaddingValues(start = 48.dp, top = 8.dp, bottom = 27.dp),
		modifier = modifier,
	) {
		activeRows.forEach { rowType ->
			val key = rowType.name

			// Header
			item(key = "${key}_header") {
				Text(
					text = rowTitle(rowType),
					style = JellyfinTheme.typography.titleMedium,
					color = JellyfinTheme.colorScheme.textPrimary,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.padding(bottom = 8.dp),
				)
			}

			// Row content — switch on type
			item(key = key) {
				when (rowType) {
					JellyseerrRowType.RECENT_REQUESTS -> MediaRow(
						items = requestItems,
						onItemClick = onItemClick,
						onItemFocused = onItemFocused,
					)
					JellyseerrRowType.TRENDING -> MediaRow(
						items = trending,
						onItemClick = onItemClick,
						onItemFocused = onItemFocused,
						onNearEnd = { discoverViewModel.loadNextTrendingPage() },
					)
					JellyseerrRowType.POPULAR_MOVIES -> MediaRow(
						items = trendingMovies,
						onItemClick = onItemClick,
						onItemFocused = onItemFocused,
						onNearEnd = { discoverViewModel.loadNextTrendingMoviesPage() },
					)
					JellyseerrRowType.UPCOMING_MOVIES -> MediaRow(
						items = upcomingMovies,
						onItemClick = onItemClick,
						onItemFocused = onItemFocused,
						onNearEnd = { discoverViewModel.loadNextUpcomingMoviesPage() },
					)
					JellyseerrRowType.POPULAR_SERIES -> MediaRow(
						items = trendingTv,
						onItemClick = onItemClick,
						onItemFocused = onItemFocused,
						onNearEnd = { discoverViewModel.loadNextTrendingTvPage() },
					)
					JellyseerrRowType.UPCOMING_SERIES -> MediaRow(
						items = upcomingTv,
						onItemClick = onItemClick,
						onItemFocused = onItemFocused,
						onNearEnd = { discoverViewModel.loadNextUpcomingTvPage() },
					)
					JellyseerrRowType.MOVIE_GENRES -> GenreRow(
						genres = movieGenres,
						mediaType = "movie",
						onGenreClick = onGenreClick,
					)
					JellyseerrRowType.SERIES_GENRES -> GenreRow(
						genres = tvGenres,
						mediaType = "tv",
						onGenreClick = onGenreClick,
					)
					JellyseerrRowType.STUDIOS -> LogoRow(
						items = studios.map { LogoItem(it.id, it.name, it.logoPath) },
						onClick = { logoItem ->
							studios.find { it.id == logoItem.id }?.let { onStudioClick(it) }
						},
					)
					JellyseerrRowType.NETWORKS -> LogoRow(
						items = networks.map { LogoItem(it.id, it.name, it.logoPath) },
						onClick = { logoItem ->
							networks.find { it.id == logoItem.id }?.let { onNetworkClick(it) }
						},
					)
				}
			}

			// Spacer between rows
			item(key = "${key}_spacer") {
				Spacer(modifier = Modifier.height(16.dp))
			}
		}
	}
}

// ── Row composables ────────────────────────────────────────────────────

@Composable
private fun MediaRow(
	items: List<JellyseerrDiscoverItemDto>,
	onItemClick: (JellyseerrDiscoverItemDto) -> Unit,
	onItemFocused: (JellyseerrDiscoverItemDto?) -> Unit,
	onNearEnd: (() -> Unit)? = null,
) {
	val rowState = rememberLazyListState()

	// Pagination trigger
	if (onNearEnd != null) {
		val shouldLoadMore by remember {
			derivedStateOf {
				val lastVisible = rowState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
				val total = rowState.layoutInfo.totalItemsCount
				total > 0 && lastVisible >= total - 10
			}
		}
		LaunchedEffect(shouldLoadMore) {
			if (shouldLoadMore) onNearEnd()
		}
	}

	LazyRow(
		state = rowState,
		horizontalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(items = items, key = { it.id }) { item ->
			JellyseerrPosterCard(
				item = item,
				onClick = { onItemClick(item) },
				onFocus = { onItemFocused(item) },
			)
		}
	}
}

@Composable
private fun GenreRow(
	genres: List<JellyseerrGenreDto>,
	mediaType: String,
	onGenreClick: (JellyseerrGenreDto, String) -> Unit,
) {
	LazyRow(
		state = rememberLazyListState(),
		horizontalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(items = genres, key = { it.id }) { genre ->
			JellyseerrGenreCard(
				genre = genre,
				onClick = { onGenreClick(genre, mediaType) },
			)
		}
	}
}

private data class LogoItem(val id: Int, val name: String, val logoUrl: String?)

@Composable
private fun LogoRow(
	items: List<LogoItem>,
	onClick: (LogoItem) -> Unit,
) {
	LazyRow(
		state = rememberLazyListState(),
		horizontalArrangement = Arrangement.spacedBy(16.dp),
	) {
		items(items = items, key = { it.id }) { item ->
			JellyseerrLogoCard(
				name = item.name,
				logoUrl = item.logoUrl,
				onClick = { onClick(item) },
			)
		}
	}
}

// ── Helpers ────────────────────────────────────────────────────────────

@Composable
private fun rowTitle(type: JellyseerrRowType): String = when (type) {
	JellyseerrRowType.RECENT_REQUESTS -> stringResource(R.string.jellyseerr_row_recent_requests)
	JellyseerrRowType.TRENDING -> stringResource(R.string.jellyseerr_row_trending)
	JellyseerrRowType.POPULAR_MOVIES -> stringResource(R.string.jellyseerr_row_popular_movies)
	JellyseerrRowType.MOVIE_GENRES -> stringResource(R.string.jellyseerr_row_movie_genres)
	JellyseerrRowType.UPCOMING_MOVIES -> stringResource(R.string.jellyseerr_row_upcoming_movies)
	JellyseerrRowType.STUDIOS -> stringResource(R.string.jellyseerr_row_studios)
	JellyseerrRowType.POPULAR_SERIES -> stringResource(R.string.jellyseerr_row_popular_series)
	JellyseerrRowType.SERIES_GENRES -> stringResource(R.string.jellyseerr_row_series_genres)
	JellyseerrRowType.UPCOMING_SERIES -> stringResource(R.string.jellyseerr_row_upcoming_series)
	JellyseerrRowType.NETWORKS -> stringResource(R.string.jellyseerr_row_networks)
}
