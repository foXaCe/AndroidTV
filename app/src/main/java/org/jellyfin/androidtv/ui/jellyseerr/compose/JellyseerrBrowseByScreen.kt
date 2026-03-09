package org.jellyfin.androidtv.ui.jellyseerr.compose

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.service.jellyseerr.JellyseerrDiscoverItemDto
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.jellyseerr.BrowseFilterType
import org.jellyfin.androidtv.ui.jellyseerr.JellyseerrDiscoverViewModel
import org.jellyfin.androidtv.ui.jellyseerr.JellyseerrSortOption
import timber.log.Timber

/**
 * Compose replacement for the Leanback VerticalGridPresenter in JellyseerrBrowseByFragment.
 * Renders a vertical grid of poster cards with sort/filter toolbar.
 */
@Composable
fun JellyseerrBrowseByScreen(
	viewModel: JellyseerrDiscoverViewModel,
	filterId: Int,
	filterName: String,
	mediaType: String,
	filterType: BrowseFilterType,
	sortOptions: List<JellyseerrSortOption>,
	onItemClick: (JellyseerrDiscoverItemDto) -> Unit,
	onItemFocused: (JellyseerrDiscoverItemDto) -> Unit,
	modifier: Modifier = Modifier,
) {
	val scope = rememberCoroutineScope()
	val gridItems = remember { mutableStateListOf<JellyseerrDiscoverItemDto>() }

	var currentPage by remember { mutableIntStateOf(1) }
	var totalPages by remember { mutableIntStateOf(1) }
	var totalResults by remember { mutableIntStateOf(0) }
	var isLoading by remember { mutableStateOf(false) }
	var currentSort by remember { mutableStateOf(sortOptions.first()) }
	var showAvailableOnly by remember { mutableStateOf(false) }
	var showRequestedOnly by remember { mutableStateOf(false) }
	var selectedTitle by remember { mutableStateOf("") }
	var selectedPosition by remember { mutableIntStateOf(0) }

	// Sort/filter menu state
	var showSortMenu by remember { mutableStateOf(false) }
	var showFilterMenu by remember { mutableStateOf(false) }

	// Content loading function
	fun loadContent(page: Int, append: Boolean = false) {
		if (isLoading) return
		isLoading = true
		scope.launch {
			try {
				val result = when (filterType) {
					BrowseFilterType.GENRE -> {
						if (mediaType == "movie") viewModel.discoverMovies(page = page, sortBy = currentSort.value, genreId = filterId.toString())
						else viewModel.discoverTv(page = page, sortBy = currentSort.value, genreId = filterId.toString())
					}
					BrowseFilterType.NETWORK -> viewModel.discoverTv(page = page, sortBy = currentSort.value, networkId = filterId.toString())
					BrowseFilterType.STUDIO -> viewModel.discoverMovies(page = page, sortBy = currentSort.value, studioId = filterId.toString())
					BrowseFilterType.KEYWORD -> {
						if (mediaType == "movie") viewModel.discoverMovies(page = page, sortBy = currentSort.value, keywords = filterId.toString())
						else viewModel.discoverTv(page = page, sortBy = currentSort.value, keywords = filterId.toString())
					}
				}
				result.getOrNull()?.let { pageResult ->
					if (!append) {
						totalPages = pageResult.totalPages
						totalResults = pageResult.totalResults
						gridItems.clear()
					}
					val filtered = applyFilter(pageResult.results, showAvailableOnly, showRequestedOnly)
					gridItems.addAll(filtered)
					currentPage = page
				}
			} catch (e: Exception) {
				Timber.e(e, "Failed to load content for $filterType: $filterName")
				if (append) currentPage = page - 1
			} finally {
				isLoading = false
			}
		}
	}

	// Initial load
	LaunchedEffect(currentSort, showAvailableOnly, showRequestedOnly) {
		currentPage = 1
		gridItems.clear()
		loadContent(1)
	}

	// Grid state for pagination
	val gridState = rememberLazyGridState()
	val shouldLoadMore by remember {
		derivedStateOf {
			val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
			val total = gridState.layoutInfo.totalItemsCount
			total > 0 && lastVisible >= total - 10 && currentPage < totalPages
		}
	}
	LaunchedEffect(shouldLoadMore) {
		if (shouldLoadMore && !isLoading) {
			loadContent(currentPage + 1, append = true)
		}
	}

	// Label helpers
	val filterTypeName = when (filterType) {
		BrowseFilterType.GENRE -> stringResource(R.string.lbl_genres)
		BrowseFilterType.NETWORK -> stringResource(R.string.jellyseerr_filter_network)
		BrowseFilterType.STUDIO -> stringResource(R.string.jellyseerr_filter_studio)
		BrowseFilterType.KEYWORD -> stringResource(R.string.jellyseerr_filter_keyword)
	}
	val mediaTypeName = if (mediaType == "movie") stringResource(R.string.lbl_movies) else stringResource(R.string.lbl_tv_series)

	Column(modifier = modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 27.dp)) {
		// ── Header ───────────────────────────────────────────────────
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
		) {
			// Selected item title (left)
			Text(
				text = selectedTitle,
				style = JellyfinTheme.typography.titleLarge,
				color = JellyfinTheme.colorScheme.textPrimary,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.weight(1f),
			)

			// Filter name (center)
			Text(
				text = filterName,
				style = JellyfinTheme.typography.titleLarge,
				color = JellyfinTheme.colorScheme.textPrimary,
				maxLines = 1,
				textAlign = TextAlign.Center,
				modifier = Modifier.weight(1f),
			)

			// Toolbar (right)
			Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
				Box {
					IconButton(onClick = { showFilterMenu = true }) {
						Icon(
							painter = painterResource(R.drawable.ic_filter),
							contentDescription = stringResource(R.string.lbl_filters),
							tint = JellyfinTheme.colorScheme.textPrimary,
						)
					}
					DropdownMenu(
						expanded = showFilterMenu,
						onDismissRequest = { showFilterMenu = false },
					) {
						DropdownMenuItem(
							text = { Text(stringResource(R.string.jellyseerr_filter_show_all)) },
							leadingIcon = { RadioButton(selected = !showAvailableOnly && !showRequestedOnly, onClick = null) },
							onClick = {
								showAvailableOnly = false
								showRequestedOnly = false
								showFilterMenu = false
							},
						)
						DropdownMenuItem(
							text = { Text(stringResource(R.string.jellyseerr_filter_available_only)) },
							leadingIcon = { RadioButton(selected = showAvailableOnly, onClick = null) },
							onClick = {
								showAvailableOnly = true
								showRequestedOnly = false
								showFilterMenu = false
							},
						)
						DropdownMenuItem(
							text = { Text(stringResource(R.string.jellyseerr_filter_requested_only)) },
							leadingIcon = { RadioButton(selected = showRequestedOnly, onClick = null) },
							onClick = {
								showAvailableOnly = false
								showRequestedOnly = true
								showFilterMenu = false
							},
						)
					}
				}

				Box {
					IconButton(onClick = { showSortMenu = true }) {
						Icon(
							painter = painterResource(R.drawable.ic_sort),
							contentDescription = stringResource(R.string.lbl_sort_by),
							tint = JellyfinTheme.colorScheme.textPrimary,
						)
					}
					DropdownMenu(
						expanded = showSortMenu,
						onDismissRequest = { showSortMenu = false },
					) {
						sortOptions.forEach { option ->
							DropdownMenuItem(
								text = { Text(option.name) },
								leadingIcon = { RadioButton(selected = option.value == currentSort.value, onClick = null) },
								onClick = {
									currentSort = option
									showSortMenu = false
								},
							)
						}
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		// ── Grid ─────────────────────────────────────────────────────
		Box(modifier = Modifier.weight(1f)) {
			LazyVerticalGrid(
				columns = GridCells.Fixed(7),
				state = gridState,
				contentPadding = PaddingValues(top = 8.dp),
				verticalArrangement = Arrangement.spacedBy(40.dp),
				horizontalArrangement = Arrangement.spacedBy(16.dp),
				modifier = Modifier.fillMaxSize().focusRestorer(),
			) {
				items(items = gridItems, key = { it.id }) { item ->
					JellyseerrPosterCard(
						item = item,
						onClick = { onItemClick(item) },
						onFocus = {
							selectedTitle = item.title ?: item.name ?: ""
							selectedPosition = gridItems.indexOf(item) + 1
							onItemFocused(item)
						},
					)
				}
			}
		}

		// ── Footer ───────────────────────────────────────────────────
		Row(
			modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(
				text = "${stringResource(R.string.lbl_showing)} $mediaTypeName ${stringResource(R.string.lbl_from)} '$filterName' ${stringResource(R.string.lbl_sorted_by)} ${currentSort.name}",
				style = JellyfinTheme.typography.labelSmall,
				color = JellyfinTheme.colorScheme.textSecondary,
				modifier = Modifier.weight(1f),
			)
			Text(
				text = "$selectedPosition | $totalResults",
				style = JellyfinTheme.typography.bodyMedium,
				color = JellyfinTheme.colorScheme.textPrimary,
			)
		}
	}
}

private fun applyFilter(
	items: List<JellyseerrDiscoverItemDto>,
	showAvailableOnly: Boolean,
	showRequestedOnly: Boolean,
): List<JellyseerrDiscoverItemDto> {
	if (!showAvailableOnly && !showRequestedOnly) return items
	return items.filter { item ->
		val status = item.mediaInfo?.status
		when {
			showAvailableOnly -> status == 4 || status == 5
			showRequestedOnly -> status == 2 || status == 3
			else -> true
		}
	}
}
