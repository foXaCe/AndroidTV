package org.jellyfin.androidtv.ui.search.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.icons.VegafoXIcons
import org.jellyfin.androidtv.ui.base.skeleton.SkeletonLandscapeCardRow
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.theme.BebasNeue
import org.jellyfin.androidtv.ui.base.theme.BrowseDimensions
import org.jellyfin.androidtv.ui.base.theme.CardDimensions
import org.jellyfin.androidtv.ui.base.theme.VegafoXColors
import org.jellyfin.androidtv.ui.base.tv.TvScaffold
import org.jellyfin.androidtv.ui.browsing.compose.BrowseMediaCard
import org.jellyfin.androidtv.ui.browsing.compose.MediaPosterCard
import org.jellyfin.androidtv.ui.itemdetail.v2.CastCard
import org.jellyfin.androidtv.ui.search.SearchResultGroup
import org.jellyfin.androidtv.ui.search.SearchViewModel
import org.jellyfin.androidtv.ui.search.composable.SearchTextInput
import org.jellyfin.androidtv.ui.search.composable.SearchVoiceInput
import org.jellyfin.androidtv.ui.shared.components.BrowseHeader
import org.jellyfin.androidtv.ui.shared.components.VegafoXScaffold
import org.jellyfin.androidtv.util.apiclient.getUrl
import org.jellyfin.androidtv.util.apiclient.itemImages
import org.jellyfin.androidtv.util.speech.rememberSpeechRecognizerAvailability
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ImageType

/**
 * Pure Compose TV search screen with category-grouped results.
 *
 * Layout:
 * - TvScaffold with overscan safe area
 * - BrowseHeader with "Search" title
 * - Voice input (optional) + text input row
 * - LazyColumn of category groups (header + LazyRow per category)
 *
 * Focus flow:
 * - Initial: focus goes to text input (or results if initialQuery provided)
 * - Submit: focus moves to results (dismisses keyboard -- critical for Fire TV)
 * - D-pad Down: navigates through category headers and rows
 * - D-pad Up: returns to input from first category
 */
@Composable
fun SearchScreen(
	viewModel: SearchViewModel,
	api: ApiClient,
	initialQuery: String?,
	onItemClick: (BaseItemDto) -> Unit,
	onItemFocus: (BaseItemDto?) -> Unit,
) {
	val results by viewModel.searchResultsFlow.collectAsState()
	val isSearching by viewModel.isSearching.collectAsState()
	var query by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
	val textInputFocusRequester = remember { FocusRequester() }
	val resultsFocusRequester = remember { FocusRequester() }
	val speechAvailable = rememberSpeechRecognizerAvailability()

	var initialFocusDone by remember { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		if (initialQuery.isNullOrBlank()) {
			textInputFocusRequester.requestFocus()
			initialFocusDone = true
		} else {
			query = query.copy(text = initialQuery)
			viewModel.searchImmediately(initialQuery)
		}
	}

	// Filter non-empty groups
	val activeGroups =
		remember(results) {
			results.filter { it.items.isNotEmpty() }
		}
	val hasResults = activeGroups.isNotEmpty()

	// Move focus to results when initial query results arrive
	LaunchedEffect(hasResults, initialFocusDone) {
		if (!initialFocusDone && hasResults) {
			initialFocusDone = true
			resultsFocusRequester.requestFocus()
		}
	}

	val isQueryEmpty = query.text.isBlank()
	val displayState =
		when {
			isQueryEmpty -> DisplayState.EMPTY
			isSearching -> DisplayState.LOADING
			!hasResults -> DisplayState.EMPTY
			else -> DisplayState.CONTENT
		}

	VegafoXScaffold {
		TvScaffold {
			Column(
				modifier =
					Modifier
						.fillMaxSize()
						.background(VegafoXColors.BackgroundDeep),
			) {
				// VegafoX header
				BrowseHeader(title = stringResource(R.string.lbl_search))

				Spacer(modifier = Modifier.height(BrowseDimensions.rowTopPadding))

				// Search input row
				Row(
					horizontalArrangement = Arrangement.spacedBy(BrowseDimensions.rowTopPadding),
					verticalAlignment = Alignment.CenterVertically,
					modifier =
						Modifier
							.focusRestorer()
							.focusGroup(),
				) {
					if (speechAvailable) {
						SearchVoiceInput(
							onQueryChange = { query = query.copy(text = it) },
							onQuerySubmit = {
								viewModel.searchImmediately(query.text)
								resultsFocusRequester.requestFocus()
							},
						)
					}

					SearchTextInput(
						query = query.text,
						onQueryChange = {
							query = query.copy(text = it)
							viewModel.searchDebounced(query.text)
						},
						onQuerySubmit = {
							viewModel.searchImmediately(query.text)
							// MUST move focus away from keyboard -- some vendors (Amazon) keep showing
							// a fullscreen keyboard otherwise, soft-locking the app.
							resultsFocusRequester.requestFocus()
						},
						modifier =
							Modifier
								.weight(1f)
								.focusRequester(textInputFocusRequester),
					)
				}

				Spacer(modifier = Modifier.height(BrowseDimensions.headerBottomSpacing))

				// Results area
				StateContainer(
					state = displayState,
					modifier =
						Modifier
							.weight(1f)
							.focusRequester(resultsFocusRequester)
							.focusGroup()
							.onFocusChanged { state ->
								if (!state.hasFocus) onItemFocus(null)
							},
					loadingContent = {
						Column {
							SkeletonLandscapeCardRow()
							SkeletonLandscapeCardRow()
						}
					},
					emptyContent = {
						EmptyState(
							title =
								if (isQueryEmpty) {
									stringResource(R.string.search_empty_hint)
								} else {
									stringResource(R.string.search_no_results, query.text)
								},
							icon = VegafoXIcons.Search,
						)
					},
					content = {
						SearchResultsList(
							groups = activeGroups,
							api = api,
							onItemClick = onItemClick,
							onItemFocus = onItemFocus,
						)
					},
				)
			}
		}
	}
}

@Composable
private fun SearchResultsList(
	groups: List<SearchResultGroup>,
	api: ApiClient,
	onItemClick: (BaseItemDto) -> Unit,
	onItemFocus: (BaseItemDto?) -> Unit,
) {
	LazyColumn(
		contentPadding = PaddingValues(bottom = BrowseDimensions.gridBottomPadding),
		modifier = Modifier.fillMaxSize(),
	) {
		groups.forEach { group ->
			val itemsList = group.items.toList()

			// Category header
			item(key = "header_${group.labelRes}") {
				SearchCategoryHeader(
					label = stringResource(group.labelRes),
					count = itemsList.size,
				)
			}

			// Category row
			item(key = "row_${group.labelRes}") {
				LazyRow(
					modifier =
						Modifier
							.focusGroup()
							.focusRestorer(),
					horizontalArrangement = Arrangement.spacedBy(BrowseDimensions.cardGap),
					contentPadding = PaddingValues(horizontal = BrowseDimensions.gridPaddingHorizontal),
				) {
					items(items = itemsList, key = { it.id }) { item ->
						SearchItemCard(
							item = item,
							group = group,
							api = api,
							onItemClick = onItemClick,
							onItemFocus = onItemFocus,
						)
					}
				}
				Spacer(modifier = Modifier.height(8.dp))
			}
		}
	}
}

@Composable
private fun SearchItemCard(
	item: BaseItemDto,
	group: SearchResultGroup,
	api: ApiClient,
	onItemClick: (BaseItemDto) -> Unit,
	onItemFocus: (BaseItemDto?) -> Unit,
) {
	val isPerson = group.kinds.contains(BaseItemKind.PERSON)
	val isSquareMusic =
		group.kinds.contains(BaseItemKind.MUSIC_ARTIST) ||
			group.kinds.contains(BaseItemKind.MUSIC_ALBUM)

	when {
		isPerson -> {
			val imageUrl =
				remember(item.id) {
					item.itemImages[ImageType.PRIMARY]
						?.getUrl(api, fillWidth = 144, quality = 96)
				}
			CastCard(
				name = item.name ?: "",
				role = null,
				imageUrl = imageUrl,
				onClick = { onItemClick(item) },
				modifier =
					Modifier.onFocusChanged { state ->
						if (state.hasFocus) onItemFocus(item)
					},
			)
		}

		isSquareMusic -> {
			val imageUrl =
				remember(item.id) {
					item.itemImages[ImageType.PRIMARY]
						?.getUrl(api, fillWidth = 280, quality = 96)
				}
			MediaPosterCard(
				imageUrl = imageUrl,
				title = item.name ?: "",
				cardWidth = CardDimensions.squareSize,
				cardHeight = CardDimensions.squareSize,
				onClick = { onItemClick(item) },
				onFocused = { onItemFocus(item) },
				subtitle =
					if (group.kinds.contains(BaseItemKind.MUSIC_ALBUM)) {
						item.albumArtist
					} else {
						null
					},
			)
		}

		else -> {
			BrowseMediaCard(
				item = item,
				api = api,
				onClick = { onItemClick(item) },
				modifier =
					Modifier.onFocusChanged { state ->
						if (state.hasFocus) onItemFocus(item)
					},
			)
		}
	}
}

@Composable
private fun SearchCategoryHeader(
	label: String,
	count: Int,
) {
	Row(
		modifier =
			Modifier
				.fillMaxWidth()
				.padding(
					start = BrowseDimensions.gridPaddingHorizontal,
					end = BrowseDimensions.gridPaddingHorizontal,
					top = 12.dp,
					bottom = 8.dp,
				),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(
			text = label,
			style =
				JellyfinTheme.typography.headlineLarge.copy(
					fontSize = 32.sp,
					fontWeight = FontWeight.Bold,
					fontFamily = BebasNeue,
				),
			color = VegafoXColors.TextPrimary,
		)
		Spacer(modifier = Modifier.width(12.dp))
		Text(
			text = "($count)",
			style = JellyfinTheme.typography.bodySmall.copy(fontSize = 14.sp),
			color = VegafoXColors.TextSecondary,
		)
	}
}
