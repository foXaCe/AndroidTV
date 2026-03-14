package org.jellyfin.androidtv.ui.browsing.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.constant.ImageType
import org.jellyfin.androidtv.constant.PosterSize
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonCard
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonCardGrid
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.ErrorState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.BrowseDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.tv.TvScaffold
import org.jellyfin.androidtv.ui.browsing.v2.FilterSortDialog
import org.jellyfin.androidtv.ui.browsing.v2.FocusedItemHud
import org.jellyfin.androidtv.ui.browsing.v2.LibraryBrowseUiState
import org.jellyfin.androidtv.ui.browsing.v2.LibraryBrowseViewModel
import org.jellyfin.androidtv.ui.browsing.v2.LibraryPosterCard
import org.jellyfin.androidtv.ui.browsing.v2.LibraryToolbarButton
import org.jellyfin.androidtv.ui.browsing.v2.PlayedStatusFilter
import org.jellyfin.androidtv.ui.browsing.v2.SeriesStatusFilter
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.ImageType as JellyfinImageType

/**
 * Compose TV screen for the library browser.
 *
 * Layout:
 * - TvScaffold with overscan safe area
 * - TvHeader: library name + item count subtitle + toolbar (home, settings)
 * - FocusedItemHud: marquee title + metadata for focused item
 * - LazyRow filter chips: sort, favorites, played status, series status, A-Z letters
 * - StateContainer: loading skeleton / empty / error / paginated grid
 *
 * Zero Leanback imports.
 */
@Composable
fun LibraryBrowseScreen(
	viewModel: LibraryBrowseViewModel,
	onItemClick: (BaseItemDto) -> Unit,
	onItemFocus: (BaseItemDto) -> Unit,
	onHomeClick: () -> Unit,
	onSettingsClick: () -> Unit,
) {
	val uiState by viewModel.uiState.collectAsState()
	var showFilterDialog by remember { mutableStateOf(false) }

	val displayState =
		when {
			uiState.isLoading && uiState.items.isEmpty() -> DisplayState.LOADING
			uiState.error != null && uiState.items.isEmpty() -> DisplayState.ERROR
			uiState.items.isEmpty() -> DisplayState.EMPTY
			else -> DisplayState.CONTENT
		}

	TvScaffold {
		Column(
			modifier =
				Modifier
					.fillMaxSize()
					.background(VegafoXColors.BackgroundDeep),
		) {
			// VegafoX library header
			Row(
				modifier =
					Modifier
						.fillMaxWidth()
						.padding(top = 32.dp, start = BrowseDimensions.gridPaddingHorizontal, end = BrowseDimensions.gridPaddingHorizontal),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = uiState.libraryName,
						style =
							JellyfinTheme.typography.headlineLarge.copy(
								fontSize = 40.sp,
								fontWeight = FontWeight.Bold,
								fontFamily = BebasNeue,
								letterSpacing = 2.sp,
							),
						color = VegafoXColors.TextPrimary,
					)
					if (uiState.totalItems > 0) {
						Text(
							text = pluralStringResource(R.plurals.items, uiState.totalItems, uiState.totalItems),
							style = JellyfinTheme.typography.bodyMedium.copy(fontSize = 14.sp),
							color = VegafoXColors.TextSecondary,
						)
					}
				}
				LibraryToolbarButton(
					icon = VegafoXIcons.Home,
					contentDescription = stringResource(R.string.home),
					onClick = onHomeClick,
				)
				LibraryToolbarButton(
					icon = VegafoXIcons.Settings,
					contentDescription = stringResource(R.string.lbl_settings),
					onClick = onSettingsClick,
				)
			}

			FocusedItemHud(
				item = uiState.focusedItem,
				modifier = Modifier.fillMaxWidth(),
			)

			Spacer(modifier = Modifier.height(8.dp))

			// Filter chips row
			FilterChipsRow(
				uiState = uiState,
				onSortClick = { showFilterDialog = true },
				onToggleFavorites = { viewModel.toggleFavorites() },
				onPlayedStatusClick = { viewModel.setPlayedFilter(it) },
				onSeriesStatusClick = { viewModel.setSeriesStatusFilter(it) },
				onLetterSelected = { viewModel.setStartLetter(it) },
			)

			Spacer(modifier = Modifier.height(12.dp))

			StateContainer(
				state = displayState,
				modifier = Modifier.weight(1f),
				loadingContent = {
					SkeletonCardGrid()
				},
				emptyContent = {
					EmptyState(
						title = stringResource(R.string.state_empty_library),
						message = stringResource(R.string.state_empty_library_message),
					)
				},
				errorContent = {
					ErrorState(
						message = stringResource(uiState.error?.messageRes ?: R.string.state_error_generic),
						onRetry = { viewModel.retry() },
					)
				},
				content = {
					LibraryContentGrid(
						uiState = uiState,
						viewModel = viewModel,
						onItemClick = onItemClick,
						onItemFocus = onItemFocus,
					)
				},
			)
		}
	}

	// Filter/Sort dialog
	if (showFilterDialog) {
		FilterSortDialog(
			title = stringResource(R.string.lbl_sort_and_filter),
			sortOptions = viewModel.sortOptions,
			currentSort = uiState.currentSortOption,
			filterFavorites = uiState.filterFavorites,
			filterPlayedStatus = uiState.filterPlayed,
			filterSeriesStatus = uiState.filterSeriesStatus,
			showPlayedStatus =
				uiState.collectionType == CollectionType.MOVIES ||
					uiState.collectionType == CollectionType.TVSHOWS,
			showSeriesStatus = uiState.collectionType == CollectionType.TVSHOWS,
			onSortSelected = { viewModel.setSortOption(it) },
			onToggleFavorites = { viewModel.toggleFavorites() },
			onPlayedStatusSelected = { viewModel.setPlayedFilter(it) },
			onSeriesStatusSelected = { viewModel.setSeriesStatusFilter(it) },
			onDismiss = { showFilterDialog = false },
		)
	}
}

// ──────────────────────────────────────────────
// Filter chips row
// ──────────────────────────────────────────────

@Composable
private fun FilterChipsRow(
	uiState: LibraryBrowseUiState,
	onSortClick: () -> Unit,
	onToggleFavorites: () -> Unit,
	onPlayedStatusClick: (PlayedStatusFilter) -> Unit,
	onSeriesStatusClick: (SeriesStatusFilter) -> Unit,
	onLetterSelected: (String?) -> Unit,
) {
	LazyRow(
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		// Sort chip
		item(key = "sort") {
			FilterChip(
				label = stringResource(uiState.currentSortOption.nameRes),
				icon = VegafoXIcons.Sort,
				onClick = onSortClick,
			)
		}

		// Favorites chip
		item(key = "favorites") {
			FilterChip(
				label = stringResource(R.string.lbl_favorites),
				isActive = uiState.filterFavorites,
				onClick = onToggleFavorites,
			)
		}

		// Played status chip (for movies/tvshows)
		if (uiState.collectionType == CollectionType.MOVIES ||
			uiState.collectionType == CollectionType.TVSHOWS
		) {
			item(key = "played") {
				val nextFilter =
					when (uiState.filterPlayed) {
						PlayedStatusFilter.ALL -> PlayedStatusFilter.UNWATCHED
						PlayedStatusFilter.UNWATCHED -> PlayedStatusFilter.WATCHED
						PlayedStatusFilter.WATCHED -> PlayedStatusFilter.ALL
					}
				FilterChip(
					label = stringResource(uiState.filterPlayed.labelRes),
					isActive = uiState.filterPlayed != PlayedStatusFilter.ALL,
					onClick = { onPlayedStatusClick(nextFilter) },
				)
			}
		}

		// Series status chip (for tvshows)
		if (uiState.collectionType == CollectionType.TVSHOWS) {
			item(key = "series_status") {
				val nextStatus =
					when (uiState.filterSeriesStatus) {
						SeriesStatusFilter.ALL -> SeriesStatusFilter.CONTINUING
						SeriesStatusFilter.CONTINUING -> SeriesStatusFilter.ENDED
						SeriesStatusFilter.ENDED -> SeriesStatusFilter.ALL
					}
				FilterChip(
					label = stringResource(uiState.filterSeriesStatus.labelRes),
					isActive = uiState.filterSeriesStatus != SeriesStatusFilter.ALL,
					onClick = { onSeriesStatusClick(nextStatus) },
				)
			}
		}

		// Letter chips (only when sorted by name)
		if (uiState.currentSortOption.sortBy == ItemSortBy.SORT_NAME) {
			val letters = listOf("#") + ('A'..'Z').map { it.toString() }
			items(letters, key = { "letter_$it" }) { letter ->
				val isSelected =
					when {
						letter == "#" && uiState.startLetter == null -> true
						letter == uiState.startLetter -> true
						else -> false
					}
				LetterChip(
					letter = letter,
					isSelected = isSelected,
					onClick = {
						if (letter == "#") {
							onLetterSelected(null)
						} else {
							onLetterSelected(letter)
						}
					},
				)
			}
		}
	}
}

// ──────────────────────────────────────────────
// Filter chip
// ──────────────────────────────────────────────

@Composable
private fun FilterChip(
	label: String,
	isActive: Boolean = false,
	icon: ImageVector? = null,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.04f else 1f,
		animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
		label = "filterChipScale",
	)

	val bgColor =
		when {
			isActive -> VegafoXColors.OrangeSoft
			else -> Color.White.copy(alpha = 0.06f)
		}

	val borderColor =
		when {
			isFocused -> VegafoXColors.OrangePrimary.copy(alpha = 0.60f)
			isActive -> VegafoXColors.OrangePrimary
			else -> Color.White.copy(alpha = 0.10f)
		}

	val contentColor =
		when {
			isActive -> VegafoXColors.OrangePrimary
			else -> VegafoXColors.TextSecondary
		}

	Box(
		modifier =
			modifier
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}.border(1.dp, borderColor, RoundedCornerShape(50.dp))
				.clip(RoundedCornerShape(50.dp))
				.background(bgColor)
				.clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				).focusable(interactionSource = interactionSource)
				.padding(horizontal = 16.dp, vertical = 8.dp),
		contentAlignment = Alignment.Center,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(6.dp),
		) {
			if (icon != null) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					modifier = Modifier.size(16.dp),
					tint = contentColor,
				)
			}
			Text(
				text = label,
				style = JellyfinTheme.typography.bodySmall.copy(fontSize = 13.sp),
				fontWeight = if (isActive) FontWeight.W600 else FontWeight.W400,
				color = contentColor,
				maxLines = 1,
			)
		}
	}
}

// ──────────────────────────────────────────────
// Letter chip (A-Z picker)
// ──────────────────────────────────────────────

@Composable
private fun LetterChip(
	letter: String,
	isSelected: Boolean,
	onClick: () -> Unit,
) {
	val interactionSource = remember { MutableInteractionSource() }
	val isFocused by interactionSource.collectIsFocusedAsState()

	val scale by animateFloatAsState(
		targetValue = if (isFocused) 1.04f else 1f,
		animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
		label = "letterChipScale",
	)

	val bgColor =
		when {
			isSelected -> VegafoXColors.OrangePrimary
			else -> Color.White.copy(alpha = 0.06f)
		}

	val borderColor =
		when {
			isFocused -> VegafoXColors.OrangePrimary.copy(alpha = 0.60f)
			isSelected -> VegafoXColors.OrangePrimary
			else -> Color.White.copy(alpha = 0.10f)
		}

	val textColor =
		when {
			isSelected -> Color.White
			else -> VegafoXColors.TextSecondary
		}

	Box(
		modifier =
			Modifier
				.graphicsLayer {
					scaleX = scale
					scaleY = scale
				}.size(32.dp)
				.border(1.dp, borderColor, RoundedCornerShape(50.dp))
				.clip(RoundedCornerShape(50.dp))
				.background(bgColor)
				.clickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
				).focusable(interactionSource = interactionSource),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = letter,
			style = JellyfinTheme.typography.bodySmall.copy(fontSize = 13.sp),
			fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal,
			color = textColor,
		)
	}
}

// ──────────────────────────────────────────────
// Paginated grid
// ──────────────────────────────────────────────

@Composable
private fun LibraryContentGrid(
	uiState: LibraryBrowseUiState,
	viewModel: LibraryBrowseViewModel,
	onItemClick: (BaseItemDto) -> Unit,
	onItemFocus: (BaseItemDto) -> Unit,
) {
	val gridState = rememberLazyGridState()
	val firstItemFocusRequester = remember { FocusRequester() }

	val (cardWidth, cardHeight) = imageTypeToCardDimensions(uiState.posterSize, uiState.imageType)
	val columns = GridCells.Adaptive(minSize = (cardWidth + 16).dp)

	// Auto-focus first item when grid loads
	LaunchedEffect(uiState.items.isNotEmpty()) {
		if (uiState.items.isNotEmpty()) {
			try {
				firstItemFocusRequester.requestFocus()
			} catch (_: Exception) {
			}
		}
	}

	// Infinite scroll — load more when last 10 items visible
	val shouldLoadMore by remember(uiState.items.size) {
		derivedStateOf {
			val lastIdx =
				gridState.layoutInfo.visibleItemsInfo
					.lastOrNull()
					?.index ?: 0
			lastIdx >= uiState.items.size - 10
		}
	}
	LaunchedEffect(shouldLoadMore, uiState.hasMoreItems) {
		if (shouldLoadMore && uiState.hasMoreItems) viewModel.loadMore()
	}

	LazyVerticalGrid(
		columns = columns,
		state = gridState,
		modifier = Modifier.fillMaxSize(),
		contentPadding = PaddingValues(bottom = 27.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		itemsIndexed(
			items = uiState.items,
			key = { _, item -> item.id },
		) { index, item ->
			LibraryPosterCard(
				item = item,
				modifier = if (index == 0) Modifier.focusRequester(firstItemFocusRequester) else Modifier,
				imageUrl = getItemImageUrl(item, uiState.imageType, viewModel.effectiveApi),
				cardWidth = cardWidth,
				cardHeight = cardHeight,
				onClick = { onItemClick(item) },
				onFocused = { onItemFocus(item) },
				showLabels = uiState.isGenreMode,
				showBadge = uiState.isGenreMode,
			)
		}

		// Skeleton items during next page load
		if (uiState.hasMoreItems) {
			items(5, key = { "skeleton_$it" }) {
				SkeletonCard(
					width = cardWidth.dp,
					height = cardHeight.dp,
				)
			}
		}
	}
}

// ──────────────────────────────────────────────
// Helpers
// ──────────────────────────────────────────────

private fun getItemImageUrl(
	item: BaseItemDto,
	imageType: ImageType,
	api: ApiClient,
): String? {
	val jellyfinType =
		when (imageType) {
			ImageType.POSTER -> JellyfinImageType.PRIMARY
			ImageType.THUMB -> JellyfinImageType.THUMB
			ImageType.BANNER -> JellyfinImageType.BANNER
			ImageType.SQUARE -> JellyfinImageType.PRIMARY
		}
	val image = item.itemImages[jellyfinType] ?: item.itemImages[JellyfinImageType.PRIMARY]
	return image?.getUrl(api, maxHeight = 400)
}

private fun imageTypeToCardDimensions(
	posterSize: PosterSize,
	imageType: ImageType,
): Pair<Int, Int> =
	when (imageType) {
		ImageType.POSTER ->
			when (posterSize) {
				PosterSize.SMALLEST -> 100 to 150
				PosterSize.SMALL -> 120 to 180
				PosterSize.MED -> 140 to 210
				PosterSize.LARGE -> 180 to 270
				PosterSize.X_LARGE -> 220 to 330
			}
		ImageType.THUMB ->
			when (posterSize) {
				PosterSize.SMALLEST -> 160 to 90
				PosterSize.SMALL -> 190 to 107
				PosterSize.MED -> 220 to 124
				PosterSize.LARGE -> 280 to 158
				PosterSize.X_LARGE -> 340 to 191
			}
		ImageType.BANNER ->
			when (posterSize) {
				PosterSize.SMALLEST -> 300 to 52
				PosterSize.SMALL -> 360 to 62
				PosterSize.MED -> 420 to 72
				PosterSize.LARGE -> 500 to 86
				PosterSize.X_LARGE -> 600 to 103
			}
		ImageType.SQUARE ->
			when (posterSize) {
				PosterSize.SMALLEST -> 100 to 100
				PosterSize.SMALL -> 120 to 120
				PosterSize.MED -> 140 to 140
				PosterSize.LARGE -> 180 to 180
				PosterSize.X_LARGE -> 220 to 220
			}
	}
