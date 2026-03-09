package org.jellyfin.androidtv.ui.search.compose

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.state.DisplayState
import org.jellyfin.androidtv.ui.base.state.EmptyState
import org.jellyfin.androidtv.ui.base.state.StateContainer
import org.jellyfin.androidtv.ui.base.tv.TvCardGrid
import org.jellyfin.androidtv.ui.base.tv.TvHeader
import org.jellyfin.androidtv.ui.base.tv.TvScaffold
import org.jellyfin.androidtv.ui.browsing.compose.BrowseMediaCard
import org.jellyfin.androidtv.ui.search.SearchViewModel
import org.jellyfin.androidtv.ui.search.composable.SearchTextInput
import org.jellyfin.androidtv.ui.search.composable.SearchVoiceInput
import org.jellyfin.androidtv.util.speech.rememberSpeechRecognizerAvailability
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.model.api.BaseItemDto

/**
 * Pure Compose TV search screen using TV foundation components.
 *
 * Layout:
 * - TvScaffold with overscan safe area
 * - TvHeader with "Search" title
 * - Voice input (optional) + text input row
 * - TvCardGrid (5 columns) for results or EmptyState
 *
 * Focus flow:
 * - Initial: focus goes to text input (or results if initialQuery provided)
 * - Submit: focus moves to results grid (dismisses keyboard — critical for Fire TV)
 * - D-pad Up from first grid row: returns to input
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

	// Flatten all result groups into a single list for the grid
	val allItems = results.flatMap { it.items }

	// Move focus to results when initial query results arrive
	LaunchedEffect(allItems, initialFocusDone) {
		if (!initialFocusDone && allItems.isNotEmpty()) {
			initialFocusDone = true
			resultsFocusRequester.requestFocus()
		}
	}

	val isQueryEmpty = query.text.isBlank()
	val displayState = when {
		isQueryEmpty -> DisplayState.EMPTY
		allItems.isEmpty() -> DisplayState.EMPTY
		else -> DisplayState.CONTENT
	}

	TvScaffold {
		Column(modifier = Modifier.fillMaxSize()) {
			TvHeader(title = stringResource(R.string.lbl_search))

			Spacer(modifier = Modifier.height(12.dp))

			// Search input row
			Row(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
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
						// MUST move focus away from keyboard — some vendors (Amazon) keep showing
						// a fullscreen keyboard otherwise, soft-locking the app.
						resultsFocusRequester.requestFocus()
					},
					modifier = Modifier
						.weight(1f)
						.focusRequester(textInputFocusRequester),
				)
			}

			Spacer(modifier = Modifier.height(16.dp))

			// Results area
			StateContainer(
				state = displayState,
				modifier = Modifier
					.weight(1f)
					.focusRequester(resultsFocusRequester)
					.focusGroup()
					.onFocusChanged { state ->
						if (!state.hasFocus) onItemFocus(null)
					},
				emptyContent = {
					EmptyState(
						title = if (isQueryEmpty) {
							stringResource(R.string.search_empty_hint)
						} else {
							stringResource(R.string.search_no_results)
						},
						icon = ImageVector.vectorResource(R.drawable.ic_search),
					)
				},
				content = {
					TvCardGrid(
						items = allItems,
						columns = 5,
						contentPadding = PaddingValues(bottom = 27.dp),
						key = { it.id },
					) { item ->
						BrowseMediaCard(
							item = item,
							api = api,
							onClick = { onItemClick(item) },
							modifier = Modifier.onFocusChanged { state ->
								if (state.hasFocus) onItemFocus(item)
							},
						)
					}
				},
			)
		}
	}
}
