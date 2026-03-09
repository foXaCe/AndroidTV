package org.jellyfin.androidtv.ui.base.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.design.Tokens

/**
 * A single row of content with a title and items.
 *
 * @param T Item type shared across all rows.
 * @property title Row header text.
 * @property items Items displayed in this row.
 * @property key Stable key for this row (defaults to [title]).
 */
data class TvRow<T>(
	val title: String,
	val items: List<T>,
	val key: String = title,
)

/**
 * Vertical list of horizontal rows — the TV "browse" pattern.
 * Replaces Leanback's BrowseSupportFragment / RowsSupportFragment + ListRow.
 *
 * Each row has a section header ([JellyfinTheme.typography.titleMedium]) followed by
 * a horizontally scrolling [LazyRow] of items.
 *
 * - Scroll follows focus automatically (Compose BringIntoView)
 * - Each row maintains its own scroll state via [rememberLazyListState]
 *
 * @param rows List of [TvRow] to display.
 * @param contentPadding Outer padding for the vertical column.
 * @param itemContent Composable for each item (typically [TvFocusCard]).
 */
@Composable
fun <T> TvRowList(
	rows: List<TvRow<T>>,
	contentPadding: PaddingValues = PaddingValues(
		start = Tokens.Space.space3xl, top = Tokens.Space.spaceLg, bottom = Tokens.Space.spaceLg,
	),
	modifier: Modifier = Modifier,
	itemContent: @Composable (T) -> Unit,
) {
	val columnState = rememberLazyListState()

	LazyColumn(
		state = columnState,
		contentPadding = contentPadding,
		modifier = modifier,
	) {
		rows.forEach { row ->
			item(key = "${row.key}_header") {
				Text(
					text = row.title,
					style = JellyfinTheme.typography.titleMedium,
					color = JellyfinTheme.colorScheme.textPrimary,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.padding(bottom = Tokens.Space.spaceSm),
				)
			}

			item(key = row.key) {
				LazyRow(
					state = rememberLazyListState(),
					horizontalArrangement = Arrangement.spacedBy(Tokens.Space.spaceMd),
				) {
					items(
						items = row.items,
					) { item ->
						itemContent(item)
					}
				}
			}

			item(key = "${row.key}_spacer") {
				Spacer(modifier = Modifier.height(Tokens.Space.spaceMd))
			}
		}
	}
}
