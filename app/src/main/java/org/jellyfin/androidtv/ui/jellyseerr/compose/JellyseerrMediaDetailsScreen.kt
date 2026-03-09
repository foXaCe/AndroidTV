package org.jellyfin.androidtv.ui.jellyseerr.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrKeywordDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrMediaInfoDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrMovieDetailsDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrRequestDto
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrTvDetailsDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.util.toHtmlSpanned

@Composable
fun JellyseerrMediaDetailsScreen(
	selectedItem: JellyseerrDiscoverItemDto?,
	movieDetails: JellyseerrMovieDetailsDto?,
	tvDetails: JellyseerrTvDetailsDto?,
	onRequestClick: (canRequestHd: Boolean, canRequest4k: Boolean, hdStatus: Int?, status4k: Int?) -> Unit,
	onCancelClick: (pendingRequests: List<JellyseerrRequestDto>) -> Unit,
	onTrailerClick: () -> Unit,
	onPlayClick: () -> Unit,
	onCastClick: (castId: Int) -> Unit,
	onItemClick: (JellyseerrDiscoverItemDto) -> Unit,
	onKeywordClick: (keywordId: Int, keywordName: String, mediaType: String) -> Unit,
	fetchRecommendations: suspend (page: Int) -> List<JellyseerrDiscoverItemDto>,
	fetchSimilar: suspend (page: Int) -> List<JellyseerrDiscoverItemDto>,
) {
	val scrollState = rememberScrollState()
	val mediaInfo = movieDetails?.mediaInfo ?: tvDetails?.mediaInfo ?: selectedItem?.mediaInfo

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(JellyfinTheme.colorScheme.background)
			.verticalScroll(scrollState),
	) {
		// Backdrop + poster + title overlay
		BackdropWithHeaderSection(
			selectedItem = selectedItem,
			movieDetails = movieDetails,
			tvDetails = tvDetails,
			mediaInfo = mediaInfo,
		)

		// Overview + facts + action buttons
		OverviewSection(
			movieDetails = movieDetails,
			tvDetails = tvDetails,
			selectedItem = selectedItem,
			mediaInfo = mediaInfo,
			onRequestClick = onRequestClick,
			onCancelClick = onCancelClick,
			onTrailerClick = onTrailerClick,
			onPlayClick = onPlayClick,
		)

		// Cast row
		val cast = movieDetails?.credits?.cast?.take(15)
			?: tvDetails?.credits?.cast?.take(15)
			?: emptyList()
		JellyseerrCastRow(
			cast = cast,
			onCastClick = onCastClick,
			modifier = Modifier.padding(horizontal = 50.dp),
		)

		Spacer(modifier = Modifier.height(32.dp))

		// Recommendations
		PaginatedCardRow(
			headingText = stringResource(R.string.lbl_recommendations),
			emptyText = stringResource(R.string.jellyseerr_no_recommendations),
			fetchPage = fetchRecommendations,
			onItemClick = onItemClick,
			modifier = Modifier.padding(horizontal = 50.dp),
		)

		Spacer(modifier = Modifier.height(32.dp))

		// Similar
		val similarTitle = if (tvDetails != null) {
			stringResource(R.string.jellyseerr_similar_series)
		} else {
			stringResource(R.string.jellyseerr_similar_titles)
		}
		PaginatedCardRow(
			headingText = similarTitle,
			emptyText = stringResource(R.string.jellyseerr_no_similar),
			fetchPage = fetchSimilar,
			onItemClick = onItemClick,
			modifier = Modifier.padding(horizontal = 50.dp),
		)

		Spacer(modifier = Modifier.height(32.dp))

		// Keywords
		val keywords = movieDetails?.keywords ?: tvDetails?.keywords ?: emptyList()
		val mediaType = if (movieDetails != null) "movie" else "tv"
		if (keywords.isNotEmpty()) {
			KeywordsSection(
				keywords = keywords,
				mediaType = mediaType,
				onKeywordClick = onKeywordClick,
				modifier = Modifier.padding(horizontal = 50.dp),
			)
		}

		Spacer(modifier = Modifier.height(48.dp))
	}
}

@Composable
private fun BackdropWithHeaderSection(
	selectedItem: JellyseerrDiscoverItemDto?,
	movieDetails: JellyseerrMovieDetailsDto?,
	tvDetails: JellyseerrTvDetailsDto?,
	mediaInfo: JellyseerrMediaInfoDto?,
) {
	Box(modifier = Modifier.fillMaxWidth()) {
		// Backdrop image
		val backdropPath = selectedItem?.backdropPath
		if (backdropPath != null) {
			AsyncImage(
				model = ImageRequest.Builder(LocalContext.current)
					.data("https://image.tmdb.org/t/p/w1280$backdropPath")
					.build(),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxWidth()
					.height(400.dp),
			)
		} else {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(400.dp)
					.background(JellyfinTheme.colorScheme.surface),
			)
		}

		// Scrim overlay
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(400.dp)
				.background(JellyfinTheme.colorScheme.scrim),
		)

		// Gradient fade at bottom of backdrop
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(120.dp)
				.align(Alignment.BottomCenter)
				.background(
					Brush.verticalGradient(
						colors = listOf(Color.Transparent, JellyfinTheme.colorScheme.background),
					)
				),
		)

		// Poster
		val posterPath = selectedItem?.posterPath
		AsyncImage(
			model = if (posterPath != null) {
				ImageRequest.Builder(LocalContext.current)
					.data("https://image.tmdb.org/t/p/w500$posterPath")
					.build()
			} else null,
			contentDescription = null,
			contentScale = ContentScale.Fit,
			modifier = Modifier
				.padding(start = 50.dp, top = 24.dp)
				.size(width = 208.dp, height = 312.dp)
				.shadow(8.dp, JellyfinTheme.shapes.small)
				.clip(JellyfinTheme.shapes.small)
				.background(JellyfinTheme.colorScheme.surface),
		)

		// Title section overlaid to the right of poster
		Column(
			modifier = Modifier
				.padding(start = 270.dp, top = 230.dp, end = 50.dp),
		) {
			JellyseerrStatusBadge(mediaInfo = mediaInfo)

			val title = when {
				movieDetails != null -> movieDetails.title
				tvDetails != null -> tvDetails.name
				else -> selectedItem?.title
			} ?: ""
			val year = when {
				movieDetails != null -> movieDetails.releaseDate?.take(4)
				tvDetails != null -> tvDetails.firstAirDate?.take(4)
				else -> selectedItem?.releaseDate?.take(4)
			}
			val displayTitle = if (year != null) "$title ($year)" else title

			Text(
				text = displayTitle,
				style = JellyfinTheme.typography.headlineMedium,
				color = JellyfinTheme.colorScheme.textPrimary,
			)

			// Attributes line (runtime, genres)
			AttributesLine(movieDetails = movieDetails, tvDetails = tvDetails)
		}
	}
}

@Composable
private fun AttributesLine(
	movieDetails: JellyseerrMovieDetailsDto?,
	tvDetails: JellyseerrTvDetailsDto?,
) {
	val attributes = remember(movieDetails, tvDetails) {
		val list = mutableListOf<String>()
		movieDetails?.runtime?.let { list.add("$it min") }
		val genres = movieDetails?.genres?.take(3)?.map { it.name }
			?: tvDetails?.genres?.take(3)?.map { it.name }
			?: emptyList()
		list.addAll(genres)
		list
	}

	if (attributes.isNotEmpty()) {
		Text(
			text = attributes.joinToString(" \u2022 "),
			style = JellyfinTheme.typography.bodyMedium,
			color = JellyfinTheme.colorScheme.textPrimary,
			modifier = Modifier.padding(top = 8.dp),
		)
	}

	val tagline = movieDetails?.tagline?.takeIf { it.isNotEmpty() }
		?: tvDetails?.tagline?.takeIf { it.isNotEmpty() }
	if (tagline != null) {
		Text(
			text = "\"$tagline\"",
			style = JellyfinTheme.typography.bodyMedium,
			fontStyle = FontStyle.Italic,
			color = JellyfinTheme.colorScheme.textSecondary,
			modifier = Modifier.padding(top = 4.dp),
		)
	}
}

@Composable
private fun OverviewSection(
	movieDetails: JellyseerrMovieDetailsDto?,
	tvDetails: JellyseerrTvDetailsDto?,
	selectedItem: JellyseerrDiscoverItemDto?,
	mediaInfo: JellyseerrMediaInfoDto?,
	onRequestClick: (Boolean, Boolean, Int?, Int?) -> Unit,
	onCancelClick: (List<JellyseerrRequestDto>) -> Unit,
	onTrailerClick: () -> Unit,
	onPlayClick: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 50.dp)
			.padding(bottom = 24.dp),
		horizontalArrangement = Arrangement.spacedBy(32.dp),
	) {
		// Left side — overview text + action buttons (2/3 width)
		Column(modifier = Modifier.weight(2f)) {
			Spacer(modifier = Modifier.height(32.dp))

			Text(
				text = stringResource(R.string.lbl_overview),
				style = JellyfinTheme.typography.titleLarge,
				color = JellyfinTheme.colorScheme.textPrimary,
				modifier = Modifier.padding(bottom = 13.dp),
			)

			val overview = movieDetails?.overview ?: tvDetails?.overview ?: selectedItem?.overview
			val overviewDisplay = overview?.toHtmlSpanned()?.takeIf { it.isNotEmpty() }?.toString()
				?: stringResource(R.string.jellyseerr_overview_unavailable)

			Text(
				text = overviewDisplay,
				style = JellyfinTheme.typography.bodyMedium,
				color = JellyfinTheme.colorScheme.textSecondary,
			)

			Spacer(modifier = Modifier.height(24.dp))

			JellyseerrRequestButtons(
				mediaInfo = mediaInfo,
				onRequestClick = onRequestClick,
				onCancelClick = onCancelClick,
				onTrailerClick = onTrailerClick,
				onPlayClick = onPlayClick,
			)
		}

		// Right side — media facts (1/3 width)
		Column(modifier = Modifier.weight(1f)) {
			Spacer(modifier = Modifier.height(32.dp))

			JellyseerrFactsSection(
				movieDetails = movieDetails,
				tvDetails = tvDetails,
				selectedItem = selectedItem,
			)
		}
	}
}

@Composable
private fun PaginatedCardRow(
	headingText: String,
	emptyText: String,
	maxPages: Int = 3,
	fetchPage: suspend (page: Int) -> List<JellyseerrDiscoverItemDto>,
	onItemClick: (JellyseerrDiscoverItemDto) -> Unit,
	modifier: Modifier = Modifier,
) {
	val items = remember { mutableStateListOf<JellyseerrDiscoverItemDto>() }
	var currentPage by remember { mutableIntStateOf(0) }
	var isLoading by remember { mutableStateOf(false) }
	var allPagesLoaded by remember { mutableStateOf(false) }
	var isEmpty by remember { mutableStateOf(false) }
	val listState = rememberLazyListState()

	// Load first page
	LaunchedEffect(Unit) {
		try {
			val firstPage = fetchPage(1)
			if (firstPage.isNotEmpty()) {
				items.addAll(firstPage)
				currentPage = 1
			} else {
				isEmpty = true
			}
		} catch (_: Exception) {
			isEmpty = true
		}
	}

	// Paginate when scrolling near end
	LaunchedEffect(listState) {
		snapshotFlow {
			val layoutInfo = listState.layoutInfo
			val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
			val totalItems = layoutInfo.totalItemsCount
			lastVisible >= totalItems - 2
		}.collect { nearEnd ->
			if (nearEnd && !isLoading && !allPagesLoaded && currentPage > 0) {
				val nextPage = currentPage + 1
				if (nextPage > maxPages) {
					allPagesLoaded = true
					return@collect
				}
				isLoading = true
				try {
					val newItems = fetchPage(nextPage)
					if (newItems.isEmpty()) {
						allPagesLoaded = true
					} else {
						items.addAll(newItems)
						currentPage = nextPage
					}
				} catch (_: Exception) {
					allPagesLoaded = true
				}
				isLoading = false
			}
		}
	}

	Column(modifier = modifier.fillMaxWidth()) {
		Text(
			text = headingText,
			style = JellyfinTheme.typography.titleLarge,
			color = JellyfinTheme.colorScheme.textPrimary,
			modifier = Modifier.padding(bottom = 16.dp),
		)

		if (isEmpty && items.isEmpty()) {
			Text(
				text = emptyText,
				style = JellyfinTheme.typography.bodyMedium,
				color = JellyfinTheme.colorScheme.textSecondary,
			)
		} else if (items.isNotEmpty()) {
			LazyRow(
				state = listState,
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 24.dp),
			) {
				itemsIndexed(items, key = { _, item -> item.id }) { _, item ->
					CardItem(item = item, onClick = { onItemClick(item) })
				}
			}
		}
	}
}

@Composable
private fun CardItem(
	item: JellyseerrDiscoverItemDto,
	onClick: () -> Unit,
) {
	JellyseerrPosterCard(
		item = item,
		onClick = onClick,
	)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordsSection(
	keywords: List<JellyseerrKeywordDto>,
	mediaType: String,
	onKeywordClick: (keywordId: Int, keywordName: String, mediaType: String) -> Unit,
	modifier: Modifier = Modifier,
) {
	Column(modifier = modifier.fillMaxWidth()) {
		Text(
			text = stringResource(R.string.lbl_keywords),
			style = JellyfinTheme.typography.titleLarge,
			color = JellyfinTheme.colorScheme.textPrimary,
			modifier = Modifier.padding(bottom = 16.dp),
		)

		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			keywords.forEach { keyword ->
				KeywordPill(
					keyword = keyword,
					onClick = { onKeywordClick(keyword.id, keyword.name, mediaType) },
				)
			}
		}
	}
}

@Composable
private fun KeywordPill(
	keyword: JellyseerrKeywordDto,
	onClick: () -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val bgColor = if (isFocused) {
		JellyfinTheme.colorScheme.surfaceContainer
	} else {
		JellyfinTheme.colorScheme.surfaceBright
	}

	Text(
		text = keyword.name,
		style = JellyfinTheme.typography.bodyLarge,
		color = JellyfinTheme.colorScheme.textPrimary,
		modifier = Modifier
			.clip(JellyfinTheme.shapes.full)
			.background(bgColor)
			.clickable(interactionSource = interactionSource, indication = null) { onClick() }
			.focusable(interactionSource = interactionSource)
			.scale(if (isFocused) 1.05f else 1.0f)
			.padding(horizontal = 24.dp, vertical = 12.dp),
	)
}
