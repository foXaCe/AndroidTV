package org.jellyfin.androidtv.ui.base.tv

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
 * @param staggerEntrance When true, rows animate in with a stagger effect on first load.
 * @param prefetchContent Called when a row becomes visible with the items of the *next* row,
 *   allowing the caller to warm up caches (e.g. image prefetch). Runs in a coroutine.
 * @param itemContent Composable for each item (typically [TvFocusCard]).
 */
@Composable
fun <T> TvRowList(
	rows: List<TvRow<T>>,
	contentPadding: PaddingValues =
		PaddingValues(
			start = Tokens.Space.space3xl,
			top = Tokens.Space.spaceLg,
			bottom = Tokens.Space.spaceLg,
		),
	modifier: Modifier = Modifier,
	columnState: LazyListState = rememberLazyListState(),
	staggerEntrance: Boolean = false,
	prefetchContent: ((items: List<T>) -> Unit)? = null,
	itemKey: ((T) -> Any)? = null,
	itemContent: @Composable (T) -> Unit,
) {
	var entrancePlayed by rememberSaveable { mutableStateOf(!staggerEntrance) }
	val shouldAnimate = staggerEntrance && !entrancePlayed

	LazyColumn(
		state = columnState,
		contentPadding = contentPadding,
		modifier = modifier.focusRestorer(),
	) {
		rows.forEachIndexed { rowIndex, row ->
			item(key = row.key) {
				// Prefetch the next row's content when this row becomes visible
				if (prefetchContent != null && rowIndex + 1 < rows.size) {
					val nextItems = rows[rowIndex + 1].items
					LaunchedEffect(row.key) {
						prefetchContent(nextItems)
					}
				}

				val animModifier =
					if (shouldAnimate) {
						val density = LocalDensity.current
						val alpha = remember { Animatable(0f) }
						val offsetY = remember { Animatable(ENTRANCE_OFFSET_DP) }

						LaunchedEffect(Unit) {
							delay(rowIndex * STAGGER_DELAY_MS)
							launch {
								alpha.animateTo(
									1f,
									tween(ENTRANCE_DURATION_MS, easing = EntranceEasing),
								)
							}
							launch {
								offsetY.animateTo(
									0f,
									tween(ENTRANCE_DURATION_MS, easing = EntranceEasing),
								)
							}
						}

						Modifier.graphicsLayer {
							this.alpha = alpha.value
							translationY = offsetY.value * density.density
						}
					} else {
						Modifier
					}

				Column(modifier = animModifier) {
					Text(
						text = row.title,
						style = JellyfinTheme.typography.titleMedium,
						color = JellyfinTheme.colorScheme.textPrimary,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.padding(bottom = 4.dp),
					)

					LazyRow(
						modifier = Modifier.focusRestorer(),
						state = rememberLazyListState(),
						horizontalArrangement = Arrangement.spacedBy(Tokens.Space.spaceMd),
						contentPadding =
							PaddingValues(
								start = Tokens.Space.spaceSm,
								end = Tokens.Space.spaceSm,
								bottom = 8.dp,
							),
					) {
						items(items = row.items, key = itemKey) { item ->
							itemContent(item)
						}
					}

					Spacer(modifier = Modifier.height(2.dp))
				}
			}
		}
	}

	// Mark entrance animation as complete
	if (shouldAnimate && rows.isNotEmpty()) {
		LaunchedEffect(Unit) {
			delay(rows.size * STAGGER_DELAY_MS + ENTRANCE_DURATION_MS + 100L)
			entrancePlayed = true
		}
	}
}

private val EntranceEasing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private const val STAGGER_DELAY_MS = 80L
private const val ENTRANCE_DURATION_MS = 400
private const val ENTRANCE_OFFSET_DP = 30f
