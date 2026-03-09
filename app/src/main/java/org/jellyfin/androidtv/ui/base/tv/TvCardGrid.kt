package org.jellyfin.androidtv.ui.base.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import org.jellyfin.design.Tokens

/**
 * Grid of cards with D-pad navigation.
 * Replaces Leanback's HorizontalGridPresenter / VerticalGridView.
 *
 * Uses standard Compose [LazyVerticalGrid] which handles D-pad focus
 * natively when items are focusable (via [TvFocusCard]).
 *
 * - Focus restoration after navigation (via [focusRestorer])
 * - Scroll follows focus automatically (Compose BringIntoView)
 * - Stable keys via [key] prevent unnecessary recompositions
 *
 * @param items Data list to display.
 * @param columns Number of fixed columns.
 * @param contentPadding Padding around the grid content (scrollable area).
 * @param key Stable key factory for each item. Should always be provided.
 * @param itemContent Composable for each grid cell (typically [TvFocusCard]).
 */
@Composable
fun <T> TvCardGrid(
	items: List<T>,
	columns: Int = 5,
	contentPadding: PaddingValues = PaddingValues(horizontal = Tokens.Space.space3xl),
	key: ((T) -> Any)? = null,
	modifier: Modifier = Modifier,
	itemContent: @Composable (T) -> Unit,
) {
	val gridState = rememberLazyGridState()

	LazyVerticalGrid(
		columns = GridCells.Fixed(columns),
		state = gridState,
		contentPadding = contentPadding,
		verticalArrangement = Arrangement.spacedBy(Tokens.Space.spaceMd),
		horizontalArrangement = Arrangement.spacedBy(Tokens.Space.spaceMd),
		modifier = modifier
			.fillMaxSize()
			.focusRestorer(),
	) {
		items(
			items = items,
			key = key,
		) { item ->
			itemContent(item)
		}
	}
}
